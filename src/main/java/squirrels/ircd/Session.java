package squirrels.ircd;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.Message;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class Session {
  private static final Logger logger = Logger.getLogger(Session.class.getCanonicalName());

  private SessionState state = SessionState.PREREGISTRATION;
  private final long loginTimeMs;
  private long lastActivityMs;

  private final ConcurrentLinkedQueue<ByteBuffer> writes = Queues.newConcurrentLinkedQueue();

  // Provided by USER
  private String username;
  private String remoteHost;
  private String serverName;
  private String realname;

  // Provided by NICK
  private String nick;

  // Provided by AWAY
  private String awayMessage;

  private final Set<Character> mode = Sets.newTreeSet();

  // Did we /QUIT?
  private boolean closed;

  // Provided by stack
  private final String actualRemoteHost;

  // Notifications when user state changes
  private final List<DisconnectListener> disconnectListeners = Lists.newLinkedList();
  private final List<NickChangeListener> nickChangeListeners = Lists.newLinkedList();

  private final Runnable wakeup;
  private final String actualServerName;

  public Session(String remoteHost, Runnable wakeup, String actualServerName) {
    logger.info("Establishing session for " + remoteHost);
    this.actualServerName = actualServerName;
    this.wakeup = wakeup;
    this.actualRemoteHost = remoteHost;
    this.loginTimeMs = System.currentTimeMillis();
    this.lastActivityMs = this.loginTimeMs;
  }

  private enum ModeParseState {
    ADDING,
    REMOVING
  }

  // TODO: disallow touching "a" via MODE
  public void adjustMode(String adjustments) {
    if (adjustments.isEmpty()) {
      return;
    }

    ModeParseState state = ModeParseState.ADDING;
    for (int i = 0; i < adjustments.length(); i++) {
      if (adjustments.charAt(i) == '+') {
        state = ModeParseState.ADDING;
      } else if (adjustments.charAt(i) == '-') {
        state = ModeParseState.REMOVING;
      } else {
        if (state == ModeParseState.ADDING) {
          mode.add(adjustments.charAt(i));
        } else {
          mode.remove(adjustments.charAt(i));
        }
      }
    }
  }

  public void updateActivityTimestamp() {
    lastActivityMs = System.currentTimeMillis();
  }

  public long getLoginTimeMs() {
    return loginTimeMs;
  }

  public long getLastActivityMs() {
    return lastActivityMs;
  }

  public void notifyOnDisconnect(DisconnectListener listener) {
    disconnectListeners.add(listener);
  }

  public void notifyOnNickChange(NickChangeListener listener) {
    nickChangeListeners.add(listener);
  }

  public String getIdentity() {
    return String.format("%s!~%s@%s", getNick(), getUsername(), getActualRemoteHost());
  }

  public void disconnect() {
    for (DisconnectListener disconnectable : disconnectListeners) {
      disconnectable.disconnect(this);
    }
  }

  public ConcurrentLinkedQueue<ByteBuffer> getWrites() {
    return writes;
  }

  public boolean isClosed() {
    return closed;
  }

  public void close() {
    closed = true;
    wakeup.run();
  }

  public void write(Message message) {
    write(message.build());
  }

  private void write(String message) {
    logger.fine("(to:" + nick + ") Writing " + Strings2.truncate(message, 140));
    message += "\r\n";
    writes.add(ByteBuffer.wrap(message.getBytes(Charsets.UTF_8)));
    wakeup.run();
  }

  public Message confirm(String verb) {
    return new Message(this).setPrefix(this.getIdentity()).add(verb);
  }

  public Message response(Code code) {
    return new Message(this, code).setPrefix(getActualServerName());
  }

  public Message receive(String from, String verb) {
    return new Message(this).setPrefix(from).add(verb);
  }

  public SessionState getState() {
    return state;
  }

  public void setNick(String nick) {
    String oldIdentity = getIdentity();
    String oldNick = getNick();
    this.nick = nick;
    for (NickChangeListener nickChangeListener : nickChangeListeners) {
      nickChangeListener.nickChange(this, oldIdentity, oldNick);
    }
  }

  public String getMode() {
    return "+" + Joiner.on("").join(mode);
  }

  public String getActualServerName() {
    return actualServerName;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setState(SessionState state) {
    this.state = state;
  }

  public String getNick() {
    return nick;
  }

  public String getUsername() {
    return username;
  }

  public String getActualRemoteHost() {
    return actualRemoteHost.substring(0, actualRemoteHost.indexOf(":"));
  }

  public void setRemoteHost(String remoteHost) {
    this.remoteHost = remoteHost;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public String getServerName() {
    return serverName;
  }

  public String getRealname() {
    return realname;
  }

  public void setRealName(String realname) {
    this.realname = realname;
  }

  public String getAwayMessage() {
    return awayMessage;
  }

  public void setAwayMessage(String awayMessage) {
    this.awayMessage = awayMessage;
  }

  public static Function<Session, String> toNickFn() {
    return new Function<Session, String>() {
      @Override
      public String apply(Session input) {
        return input.getNick();
      }
    };
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("nick", nick)
        .add("actualRemoteHost", actualRemoteHost)
        .toString();
  }
}
