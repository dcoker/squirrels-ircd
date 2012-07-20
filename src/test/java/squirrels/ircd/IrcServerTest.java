package squirrels.ircd;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import squirrels.ircd.commands.Flood;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IrcServerTest {

  private IrcServer server;
  private Session session;

  @Before
  public void setUp() throws Exception {
    Runnable wakeup = new Runnable() {
      @Override
      public void run() {
      }
    };
    session = new Session("remotehost", wakeup, "actual");
  }

  private static int findUnusedPort() {
    Random random = new Random();
    int portNumber = -1;
    boolean portTaken = true;
    while (portTaken) {
      portNumber = random.nextInt(64000 - 1025) + 1025;
      try (ServerSocket socket = new ServerSocket(portNumber)) {
        socket.setReuseAddress(true);
        portTaken = false;
      } catch (IOException e) {
        portTaken = true;
      }
    }
    return portNumber;
  }

  static class Flooder implements Callable<IrcClient> {
    private final CountDownLatch latch;
    private final int id;
    private final int port;

    Flooder(CountDownLatch latch, int id, int port) {
      this.latch = latch;
      this.id = id;
      this.port = port;
    }

    @Override
    public IrcClient call() throws Exception {
      latch.await();
      IrcClient client = new IrcClient(id, "127.0.0.1", port);
      client.hello();
      client.flood();
      return client;
    }
  }

  @Test(timeout = 60000)
  public void flood() throws IOException, InterruptedException, ExecutionException {
    int nThreads = 25;
    final Flags flags = new Flags();
    flags.port = findUnusedPort();
    server = new IrcServer(flags);
    Thread serverThread = new Thread(server);
    serverThread.start();
    try {
      ExecutorService executors = Executors.newFixedThreadPool(nThreads);
      List<Future<IrcClient>> clients = Lists.newLinkedList();
      CountDownLatch latch = new CountDownLatch(1);
      for (int i = 0; i < nThreads; i++) {
        clients.add(executors.submit(new Flooder(latch, i, flags.port)));
      }
      // start them all at once and give them all time to finish
      latch.countDown();
      for (Future<IrcClient> clientFuture : clients) {
        try (IrcClient client = clientFuture.get()) {
          String string;
          int numMessagesToExpect = Flood.NUM_MESSAGES;
          while (numMessagesToExpect > 0 && (string = client.getReader().readLine()) != null) {
            if (string.contains("PRIVMSG")) {
              numMessagesToExpect--;
              assertTrue(string.contains(":{") && string.endsWith("}"));
            }
          }
          assertEquals(0, numMessagesToExpect);
        }
      }
    } finally {
      serverThread.interrupt();
    }
  }

  @Test
  public void trimCarriageReturn() {
    Map<String, Integer> expectations = ImmutableMap.<String, Integer>builder()
        .put("", 0)
        .put("\r", 0)
        .put("\r\r", 0)
        .put("x", 1)
        .put("x\r", 1)
        .put("xx", 2)
        .put("xx\r", 2)
        .put("xx\r\r", 2)
        .build();
    for (String payload : expectations.keySet()) {
      ByteBuffer buffer = ByteBuffer.wrap(payload.getBytes(Charsets.UTF_8));
      IrcServer.trimCarriageReturn(buffer);
      assertEquals((int) expectations.get(payload), buffer.limit());
    }
  }

  @Test
  public void handleReadReusesByteBuffer() throws Exception {
    String largeLine = Strings.repeat("X", 511);
    String largeLineWithNl = Strings.repeat("X", 510) + "\n";
    List<TestInput> expectations = ImmutableList.<TestInput>builder().add(
        new TestInput(""),
        new TestInput("ONE"),
        new TestInput("\n", "ONE"),
        new TestInput("T"),
        new TestInput("WO\r\n", "TWO"),
        new TestInput("A\nB\r\nC\n", "A", "B", "C"),
        new TestInput(""),
        new TestInput("X"),
        new TestInput("YZ\nPQR\n", "XYZ", "PQR"),
        new TestInput(largeLine),
        new TestInput("\n", largeLine),
        new TestInput(largeLineWithNl, largeLineWithNl.trim()),
        new TestInput("\n", ""),
        new TestInput("\n\n\n\n\n\n\n\n\n\n\n\n\n",
            "", "", "", "", "", "", "", "", "", "", "", "", ""),
        new TestInput("\n", "")
    ).build();

    Protocol protocol = EasyMock.createStrictMock(Protocol.class);
    server = new IrcServer(protocol);
    ByteBuffer input = ByteBuffer.allocate(512);

    for (TestInput expectation : expectations) {
      EasyMock.reset(protocol);
      for (String command : expectation.commands) {
        protocol.process(session, command);
      }
      EasyMock.replay(protocol);

      input.put(expectation.data.getBytes(Charsets.UTF_8));
      input.flip();
      server.handleRead(session, input);
      EasyMock.verify(protocol);
    }
  }

  class TestInput {
    String data;
    String[] commands;

    TestInput(String data, String... commands) {
      this.data = data;
      this.commands = commands;
    }

    @Override
    public String toString() {
      return Objects.toStringHelper(this)
          .add("data", data)
          .add("commands", Arrays.asList(commands)).toString();
    }
  }
}
