package squirrels.ircd;

import com.beust.jcommander.JCommander;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An IRC server.
 *
 * There are two threads: The IO thread implements a classic NIO select loop that dispatches
 * IRC-specific operations to the Protocol thread. The single Protocol thread executes all user
 * actions single-threadedly, and maintains a queue of pending writes in a per-user Session object
 * which is polled by the IO thread writer upon notification via a callback invoked on the Protocol
 * thread. The IO thread directly adds a work item to the Protocol executor service for each
 * received command.
 */
public class IrcServer implements Runnable {
  private static final Logger logger = Logger.getLogger(IrcServer.class.getCanonicalName());

  private static final int MAX_COMMAND_BUF_SZ = 512;
  private static final char NL = '\n';

  /**
   * The Protocol thread. This should be a single thread (either a sameThreadExecutor() or a
   * singleton thread executor).
   */
  private final ExecutorService protocolExecutor;

  /**
   * Read buffers for each channel.
   */
  private final Map<SocketChannel, ByteBuffer> readBuffers = Maps.newHashMap();

  /**
   * Collects a list of Channels that should have their interest set updated to include OP_WRITE.
   * Updated via a callback from the Protocol thread.
   */
  private final ConcurrentLinkedQueue<SocketChannel> channelsWithWritesPending =
      Queues.newConcurrentLinkedQueue();

  /**
   * IRC charsets are broken, but we use UTF-8.
   */
  private final CharsetDecoder utf8Decoder = Charsets.UTF_8.newDecoder()
      .onMalformedInput(CodingErrorAction.REPLACE)
      .onUnmappableCharacter(CodingErrorAction.REPLACE);

  private final Flags flags;

  private final Selector selector;

  /**
   * Tracks the association between socket and Session.
   */
  private final HashBiMap<SocketChannel, Session> sessions = HashBiMap.create();

  private final SessionFactory sessionFactory;

  private final Protocol protocol;

  public static void main(String... args) throws IOException {
    Flags flags = new Flags();
    new JCommander(flags, args);

    IrcServer ircServer = new IrcServer(flags);
    ircServer.loopForever();
  }

  @VisibleForTesting
  IrcServer(Protocol protocol) {
    flags = new Flags();
    this.selector = null;
    this.protocol = protocol;
    this.sessionFactory = null;
    protocolExecutor = MoreExecutors.sameThreadExecutor();
  }

  public IrcServer(Flags flags) throws IOException {
    this.flags = flags;

    protocolExecutor = Executors.newSingleThreadExecutor();

    selector = Selector.open();
    ServerSocketChannel server = ServerSocketChannel.open();
    server.configureBlocking(false);

    InetSocketAddress address = new InetSocketAddress(flags.port);
    server.socket().bind(address);
    server.register(selector, SelectionKey.OP_ACCEPT);

    Users users = new Users();
    ChannelServiceImpl channelService = new ChannelServiceImpl();
    protocol = new Protocol(channelService, users, flags.networkName);
    sessionFactory = new SessionFactory(users, flags.serverName);
  }

  @Override
  public void run() {
    try {
      loopForever();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "loopForever threw exception", e);
    }
  }

  void loopForever() throws IOException {
    logger.info("Server listening on port " + flags.port);

    while (!Thread.interrupted()) {
      SocketChannel possibleWriter;
      while ((possibleWriter = channelsWithWritesPending.poll()) != null) {
        try {
          possibleWriter.register(selector, SelectionKey.OP_WRITE);
        } catch (ClosedChannelException ex) {
          logger.severe("Attempt to write after channel closed");
        }
      }

      Stopwatch selecting = new Stopwatch().start();
      int items = selector.select();
      selecting.stop();

      Stopwatch loop = new Stopwatch().start();
      int accept = 0;
      int read = 0;
      int write = 0;
      int invalid = 0;
      Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
      while (iterator.hasNext()) {
        SelectionKey key = iterator.next();
        iterator.remove();

        if (!key.isValid()) {
          invalid++;
          continue;
        }

        if (key.isAcceptable()) {
          accept++;
          accept(selector, key);
        } else if (key.isReadable()) {
          read++;
          read(key);
        } else if (key.isWritable()) {
          write++;
          write(key);
        }
      }
      loop.stop();
      logger.fine(String.format("tselect=%d tloop=%d keys=%d accept=%d read=%d " +
          "write=%d invalid=%d",
          selecting.elapsedMillis(),
          loop.elapsedMillis(),
          items,
          accept,
          read,
          write,
          invalid));
    }
    logger.info("Server interrupted. Done.");
  }

  private void write(SelectionKey key) throws IOException {
    SocketChannel channel = (SocketChannel) key.channel();
    Session session = sessions.get(channel);

    // Attempt to write all the ByteBuffers, in order, to the channel. If we fail to write an
    // entire buffer (suggesting the socket is full) in 10 iterations, we "yield" to the select
    // loop. Our goal is to keep the channel buffers full but not at the cost of starving other
    // connections of writes. Of course, this assumes that the selector will fairly schedule the
    // keys but we assume it is random enough. Should test this.
    ConcurrentLinkedQueue<ByteBuffer> byteBuffers = session.getWrites();
    Iterator<ByteBuffer> iterator = byteBuffers.iterator();
    boolean yield = false;
    while (iterator.hasNext() && !yield) {
      ByteBuffer buffer = iterator.next();
      int iters = 0;
      int written = 0;
      while (buffer.hasRemaining() && iters < 10) {
        iters++;
        written += channel.write(buffer);
      }

      maybeLogWriteStats(channel, buffer, iters, written);

      if (!buffer.hasRemaining()) {
        iterator.remove();
      } else {
        yield = true;
      }
    }

    if (byteBuffers.isEmpty()) {
      key.interestOps(SelectionKey.OP_READ);
    }

    if (session.isClosed()) {
      hangup(channel);
    }
  }

  private void maybeLogWriteStats(SocketChannel channel, ByteBuffer buffer, int iters, int written)
      throws SocketException {
    if (logger.isLoggable(Level.FINE)) {
      double percentage = (double) buffer.position() / buffer.limit();
      logger.fine(String.format("Wrote %6d bytes through offset %8d of %8d (%2.2f) " +
          "iters=%s remaining=%s sendbufsize=%s",
          written,
          buffer.position(), buffer.limit(), percentage, iters, buffer.hasRemaining(),
          channel.socket().getSendBufferSize()));
    }
  }

  private void read(SelectionKey next) throws IOException {
    SocketChannel channel = (SocketChannel) next.channel();
    ByteBuffer buf = readBuffers.get(channel);
    if (buf == null) {
      buf = ByteBuffer.allocate(MAX_COMMAND_BUF_SZ);
      readBuffers.put(channel, buf);
    }
    Session session = sessions.get(channel);

    int numRead;
    try {
      numRead = channel.read(buf);
    } catch (IOException ex) {
      logger.info("close during read");
      hangup(channel);
      next.cancel();
      channel.close();
      return;
    }

    if (numRead == -1) {
      logger.info("Closing " + channel);
      hangup(channel);
      next.cancel();
      channel.close();
      return;
    }

    buf.flip();
    handleRead(session, buf);
  }

  @VisibleForTesting
  void handleRead(Session session, ByteBuffer buf) {
    int lastNewline = -1;
    while (buf.position() < buf.limit()) {
      int start = buf.position();
      byte b = 0;
      while (buf.position() < buf.limit() && (b = buf.get()) != NL) {
        // seeking to first NL
      }
      if (b == NL) {
        lastNewline = buf.position();
        int oldLimit = buf.limit();
        buf.position(start);
        buf.limit(lastNewline - 1);
        handleCommand(session, buf.slice());
        buf.limit(oldLimit);
        buf.position(lastNewline);
      }
    }
    if (lastNewline != -1) {
      buf.position(lastNewline);
      buf.compact();
    } else {
      buf.limit(buf.capacity());
    }
  }

  private void handleCommand(final Session session, ByteBuffer command) {
    trimCarriageReturn(command);
    final CharBuffer decoded;
    try {
      decoded = utf8Decoder.decode(command);
    } catch (CharacterCodingException e) {
      logger.log(Level.WARNING, "Problem decoding input buffer", e);
      return;
    }

    protocolExecutor.submit(new Runnable() {
      @Override
      public void run() {
        protocol.process(session, decoded.toString());
      }
    });
  }

  @VisibleForTesting
  static void trimCarriageReturn(ByteBuffer buffer) {
    while (buffer.limit() > 0 && buffer.get(buffer.limit() - 1) == '\r') {
      buffer.limit(buffer.limit() - 1);
    }
  }

  private void hangup(SocketChannel channel) throws IOException {
    final Session session = sessions.get(channel);
    Future<?> disconnected = protocolExecutor.submit(new Runnable() {
      @Override
      public void run() {
        session.disconnect();
      }
    });
    Futures.getUnchecked(disconnected);
    sessions.remove(channel);
    channel.close();
  }

  private void accept(Selector selector, SelectionKey next) throws IOException {
    ServerSocketChannel serverChannel = (ServerSocketChannel) next.channel();
    SocketChannel channel = serverChannel.accept();
    channel.configureBlocking(false);
    channel.register(selector, SelectionKey.OP_READ);
    final Session session = sessionFactory.newSession(channel,
        new WriteReadyCallback(selector, channel));
    sessions.put(channel, session);
    protocolExecutor.submit(new Runnable() {
      @Override
      public void run() {
        protocol.hello(session);
      }
    });
  }

  /**
   * Invoked by Protocol thread when there are writes ready to be written, or by IO thread when we
   * are handling connection close.
   */
  private class WriteReadyCallback implements Runnable {
    private final Selector selector;
    private final SocketChannel channel;

    private WriteReadyCallback(Selector selector, SocketChannel channel) {
      this.channel = channel;
      this.selector = selector;
    }

    @Override
    public void run() {
      channelsWithWritesPending.add(channel);
      selector.wakeup();
    }
  }
}
