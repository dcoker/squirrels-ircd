package squirrels.ircd;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

class SessionFactory {
  private final Users users;
  private final String serverName;

  SessionFactory(Users users, String serverName) {
    this.users = users;
    this.serverName = serverName;
  }

  Session newSession(final SocketChannel channel, Runnable writeCallback) {
    InetSocketAddress remoteSocketAddress =
        (InetSocketAddress) channel.socket().getRemoteSocketAddress();
    String remoteHostAddress =
        remoteSocketAddress.getAddress().getHostAddress() + ":" + remoteSocketAddress.getPort();
    return newSession(writeCallback, remoteHostAddress);
  }

  private Session newSession(Runnable writeCallback, String remoteHostAddress) {
    Session session = new Session(remoteHostAddress, writeCallback, serverName);
    session.notifyOnDisconnect(users);
    session.notifyOnNickChange(users);
    return session;
  }
}
