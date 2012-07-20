package squirrels.ircd.commands;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;
import squirrels.ircd.ChannelService;
import squirrels.ircd.SessionState;
import squirrels.ircd.Users;
import squirrels.ircd.messages.ParsedMessage;

import java.util.Map;

public class CommandFactoryTest {
  private final Users users = null;
  private final ChannelService channelService = null;

  @Test
  public void construct() {
    CommandFactory factory = new CommandFactory(users, channelService);
    AbstractCommand command = factory.newCommand(SessionState.REGISTERED, ParsedMessage.parse(
        ":source NICK nick"));
    Assert.assertTrue(command instanceof Nick);
  }

  @Test
  public void unknown() {
    CommandFactory factory = new CommandFactory(users, channelService);
    AbstractCommand command = factory.newCommand(SessionState.REGISTERED,
        ParsedMessage.parse("SAUSAGES"));
    Assert.assertTrue(command instanceof Unknown);
  }

  @Test
  public void states() {
    CommandFactory factory = new CommandFactory(users, channelService);
    AbstractCommand command = factory.newCommand(SessionState.PREREGISTRATION,
        ParsedMessage.parse(":source JOIN nick"));
    Assert.assertTrue(command instanceof Unknown);
  }

  @Test
  public void classes() {
    Map<String, Class<? extends AbstractCommand>> expectations =
        ImmutableMap.<String, Class<? extends AbstractCommand>>builder()
            .put("AWAY", Away.class)
            .put("JOIN", Join.class)
            .put("LIST", List.class)
            .put("LUSERS", Lusers.class)
            .put("MODE", Mode.class)
            .put("NAMES", Names.class)
            .put("NICK", Nick.class)
            .put("PART", Part.class)
            .put("PING", Ping.class)
            .put("PRIVMSG", Privmsg.class)
            .put("QUIT", Quit.class)
            .put("TOPIC", Topic.class)
            .put("USER", User.class)
            .put("WHOIS", Whois.class)
            .put("WHO", Who.class)
            .put("WHOWAS", Whowas.class)
            .put("somerandomclass", Unknown.class)
            .build();
    CommandFactory factory = new CommandFactory(null, null);

    for (String verb : expectations.keySet()) {
      ParsedMessage pm = ParsedMessage.parse(verb);
      Assert.assertSame(expectations.get(verb),
          factory.newCommand(SessionState.REGISTERED, pm).getClass());
    }
  }
}
