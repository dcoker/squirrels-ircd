package squirrels.ircd.commands;

import org.junit.Test;
import squirrels.ircd.messages.ParsedMessage;

public class PrivmsgTest extends BaseCommandTest {
  @Test
  public void invalid() {
    Privmsg privmsg = new Privmsg(users, channels);
    privmsg.execute(ParsedMessage.parse("PRIVMSG "), session);
    assertResponses(session, ":realservername 461 usernick PRIVMSG :Not enough parameters");
  }

  @Test
  public void user2user() {
    users.nickChange(session2, null, null);
    Privmsg privmsg = new Privmsg(users, channels);
    ParsedMessage command = ParsedMessage.parse("PRIVMSG usernick2 :hello::!");
    privmsg.execute(command, session);
    assertResponses(session2, ":usernick!~null@remotehost PRIVMSG usernick2 :hello::!");
  }

  @Test
  public void user2missinguser() {
    Privmsg privmsg = new Privmsg(users, channels);
    privmsg.execute(ParsedMessage.parse("PRIVMSG usernick2 :hello::!"), session);
    assertResponses(session, ":realservername 401 usernick usernick2 :No such nick/channel");
  }

  @Test
  public void user2awayuser() {
    users.nickChange(session2, null, null);
    session2.setAwayMessage("diving");
    Privmsg privmsg = new Privmsg(users, channels);
    privmsg.execute(ParsedMessage.parse("PRIVMSG usernick2 :hello::!"), session);
    assertResponses(session, ":realservername 301 usernick usernick2 :diving");
    assertResponses(session2, ":usernick!~null@remotehost PRIVMSG usernick2 :hello::!");
  }

  @Test
  public void user2channel() {
    channels.getOrCreate("#a").join(session);
    reset();
    Privmsg privmsg = new Privmsg(users, channels);
    privmsg.execute(ParsedMessage.parse("PRIVMSG #a :hello::!"), session);
    assertResponses(session);
  }

  @Test
  public void user2missingchannel() {
    Privmsg privmsg = new Privmsg(users, channels);
    privmsg.execute(ParsedMessage.parse("PRIVMSG #a :hello::!"), session);
    assertResponses(session, ":realservername 403 usernick #a :No such channel");
  }
}
