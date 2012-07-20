package squirrels.ircd;

import java.io.*;
import java.net.Socket;

@SuppressWarnings("SocketOpenedButNotSafelyClosed")
public class IrcClient implements Closeable {
  private static final String REAL_NAME = "Test Flood Client";

  private final String nick;
  private final String server;
  private final Socket socket;
  private final OutputStreamWriter output;
  private final BufferedReader reader;

  @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
  public IrcClient(int nickNum, String server, int port) throws IOException {
    this.server = server;
    this.nick = "nick" + nickNum;
    socket = new Socket(server, port);
    output = new OutputStreamWriter(socket.getOutputStream());
    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
  }

  public void hello() throws IOException {
    send("NICK %s", nick);
    send("USER %s %s %s :%s", nick, nick, server, REAL_NAME);
  }

  public void flood() throws IOException {
    send("MODE %s +O", nick);
    send("FLOOD");
  }

  public void send(String message, Object... args) throws IOException {
    output.write(String.format(message, args) + "\n");
    output.flush();
  }

  public BufferedReader getReader() {
    return reader;
  }

  public void close() throws IOException {
    socket.close();
  }
}
