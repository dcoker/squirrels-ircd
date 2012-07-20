package squirrels.ircd.commands;

import org.junit.Test;
import squirrels.ircd.messages.ParsedMessage;

public class PingTest extends BaseCommandTest {
  @Test
  public void prereg() {
    Ping ping = new Ping();
    ping.execute(ParsedMessage.parse("PING :6DD805EE"), session);
    assertResponses(session, "PONG :6DD805EE");
  }

  @Test
  public void postreg() {
    Ping ping = new Ping();
    ping.execute(ParsedMessage.parse("PING irc.choopa.net"), session);
    assertResponses(session, ":irc.choopa.net PONG irc.choopa.net :irc.choopa.net");
  }
}
