package squirrels.ircd.commands;

import org.junit.Assert;
import org.junit.Test;
import squirrels.ircd.messages.ParsedMessage;

public class PartTest extends BaseCommandTest {
  @Test
  public void part() {
    channels.getOrCreate("#a").join(session);
    channels.getOrCreate("#b").join(session);
    session.getWrites().clear();
    wakeupCount.set(0);

    Part part = new Part(channels);
    part.execute(ParsedMessage.parse("PART #a,#b"), session);
    Assert.assertNull(channels.get("#a"));
    Assert.assertNull(channels.get("#b"));
    assertResponses(session,
        ":usernick!~null@remotehost PART #a",
        ":usernick!~null@remotehost PART #b");
  }
}
