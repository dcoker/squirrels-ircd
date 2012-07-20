package squirrels.ircd.messages;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public final class ParsedMessageTest {

  @Parameterized.Parameters
  public static Collection<Object[]> configs() {
    Object[][] objects = {
        {"VERB", new ParsedMessage(null, "VERB", null, null)},
        {":prefix VERB", new ParsedMessage("prefix", "VERB", null, null)},
        {":prefix VERB :final", new ParsedMessage("prefix", "VERB", null, "final")},
        {"USER username 0 * :Real name",
            new ParsedMessage(null, "USER", new String[]{"username", "0", "*"}, "Real name")},
        {"USER username 0 * :",
            new ParsedMessage(null, "USER", new String[]{"username", "0", "*"}, "")},
        {"USER username 0 *",
            new ParsedMessage(null, "USER", new String[]{"username", "0", "*"}, null)},
        {"NICK name",
            new ParsedMessage(null, "NICK", new String[]{"name"}, null)},
        {":source NICK name",
            new ParsedMessage("source", "NICK", new String[]{"name"}, null)},
        {":source JOIN",
            new ParsedMessage("source", "JOIN", null, null)},
        {":source JOIN ",
            new ParsedMessage("source", "JOIN", null, null)},
        {":source JOIN :",
            new ParsedMessage("source", "JOIN", null, "")},
        {":source JOIN :#channel",
            new ParsedMessage("source", "JOIN", null, "#channel")},
        {":source JOIN :#channel,#channel2",
            new ParsedMessage("source", "JOIN", null, "#channel,#channel2")},
        {":source PRIVMSG <target> :Message",
            new ParsedMessage("source", "PRIVMSG", new String[]{"<target>"}, "Message")},
        {":source PRIVMSG <target> :Message:ahoy",
            new ParsedMessage("source", "PRIVMSG", new String[]{"<target>"}, "Message:ahoy")},
        {"PRIVMSG <target> :Message",
            new ParsedMessage(null, "PRIVMSG", new String[]{"<target>"}, "Message")},
        {"QUIT :reason",
            new ParsedMessage(null, "QUIT", null, "reason")},
        {"JOIN :reason",
            new ParsedMessage(null, "JOIN", null, "reason")},
        {"QUIT",
            new ParsedMessage(null, "QUIT", null, null)},
    };
    return Arrays.asList(objects);
  }

  private final String command;
  private final ParsedMessage expected;

  public ParsedMessageTest(String command, ParsedMessage expected) {
    this.command = command;
    this.expected = expected;
  }

  @Test
  public void test() {
    ParsedMessage parsed = ParsedMessage.parse(command);
    assertEquals(expected, parsed);
  }
}

