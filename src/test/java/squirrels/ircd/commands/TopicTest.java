package squirrels.ircd.commands;

import org.junit.Test;
import squirrels.ircd.messages.ParsedMessage;

public class TopicTest extends BaseCommandTest {
  @Test
  public void brian() {
    channels.getOrCreate("#woof").setTopic(session, "tails");
    reset();
    Topic topic = new Topic(channels);
    topic.execute(ParsedMessage.parse("TOPIC #woof"), session);
    assertResponses(session, ":realservername 331 usernick #woof :tails");
  }
}
