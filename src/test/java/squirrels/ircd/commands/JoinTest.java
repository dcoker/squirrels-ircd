package squirrels.ircd.commands;

import org.junit.Test;
import squirrels.ircd.messages.ParsedMessage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JoinTest extends BaseCommandTest {

  @Test
  public void success() {
    Join join = new Join(channels);
    join.execute(ParsedMessage.parse("JOIN #a"), session);
    assertEquals(3, wakeupCount.get());
    assertTrue(channels.get("#a").contains(session));
    assertResponses(session,
        ":usernick!~null@remotehost JOIN :#a",
        ":realservername 353 usernick = #a :usernick",
        ":realservername 366 usernick #a :End of /NAMES list");
  }

  @Test
  public void successAlternateSyntax() {
    Join join = new Join(channels);
    join.execute(ParsedMessage.parse("JOIN :#a"), session);
    assertEquals(3, wakeupCount.get());
    assertTrue(channels.get("#a").contains(session));
    assertResponses(session,
        ":usernick!~null@remotehost JOIN :#a",
        ":realservername 353 usernick = #a :usernick",
        ":realservername 366 usernick #a :End of /NAMES list");
  }

  @Test
  public void successMultiple() {
    Join join = new Join(channels);
    join.execute(ParsedMessage.parse("JOIN #a,#b"), session);
    assertEquals(6, wakeupCount.get());
    assertTrue(channels.get("#a").contains(session));
    assertTrue(channels.get("#b").contains(session));
    assertEquals(2, channels.list().size());
    assertResponses(session,
        ":usernick!~null@remotehost JOIN :#a",
        ":realservername 353 usernick = #a :usernick",
        ":realservername 366 usernick #a :End of /NAMES list",
        ":usernick!~null@remotehost JOIN :#b",
        ":realservername 353 usernick = #b :usernick",
        ":realservername 366 usernick #b :End of /NAMES list");
  }

  @Test
  public void insufficientArgs() {
    Join join = new Join(channels);
    join.execute(ParsedMessage.parse("JOIN"), session);
    assertEquals(1, wakeupCount.get());
    assertEquals(0, channels.list().size());
    assertResponses(session, ":realservername 461 usernick JOIN :Not enough parameters");
  }

  @Test
  public void invalidChannel() {
    Join join = new Join(channels);
    join.execute(ParsedMessage.parse("JOIN #"), session);
    assertEquals(1, wakeupCount.get());
    assertEquals(0, channels.list().size());
    assertResponses(session, ":realservername 403 usernick # :No such channel");
  }
}
