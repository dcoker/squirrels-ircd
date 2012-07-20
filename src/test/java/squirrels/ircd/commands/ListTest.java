package squirrels.ircd.commands;

import org.junit.Test;
import squirrels.ircd.messages.ParsedMessage;

public class ListTest extends BaseCommandTest {
  @Test
  public void empty() {
    List list = new List(channels);
    list.execute(ParsedMessage.parse("LIST"), session);
    assertResponses(session,
        ":realservername 321 usernick :Begin of /LIST",
        ":realservername 323 usernick :End of /LIST");
  }

  @Test
  public void one() {
    channels.getOrCreate("#a");
    List list = new List(channels);
    list.execute(ParsedMessage.parse("LIST"), session);
    assertResponses(session,
        ":realservername 321 usernick :Begin of /LIST",
        ":realservername 322 usernick #a 0",
        ":realservername 323 usernick :End of /LIST");
  }

  @Test
  public void two() {
    channels.getOrCreate("#a").setTopic(session, "newtopic");
    channels.getOrCreate("#b");

    {
      List list = new List(channels);
      list.execute(ParsedMessage.parse("LIST"), session);
      assertResponses(session,
          ":realservername 321 usernick :Begin of /LIST",
          ":realservername 322 usernick #a 0 newtopic",
          ":realservername 322 usernick #b 0",
          ":realservername 323 usernick :End of /LIST");
    }

    {
      List list = new List(channels);
      list.execute(ParsedMessage.parse("LIST #a,#b"), session);
      assertResponses(session,
          ":realservername 321 usernick :Begin of /LIST",
          ":realservername 322 usernick #a 0 newtopic",
          ":realservername 322 usernick #b 0",
          ":realservername 323 usernick :End of /LIST");
    }

    {
      List list = new List(channels);
      list.execute(ParsedMessage.parse("LIST #a"), session);
      assertResponses(session,
          ":realservername 321 usernick :Begin of /LIST",
          ":realservername 322 usernick #a 0 newtopic",
          ":realservername 323 usernick :End of /LIST");
    }
  }
}
