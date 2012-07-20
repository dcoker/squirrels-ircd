package squirrels.ircd.commands;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import squirrels.ircd.ChannelService;
import squirrels.ircd.SessionState;
import squirrels.ircd.Users;
import squirrels.ircd.messages.ParsedMessage;

public class CommandFactory {
  private static final Multimap<SessionState, String> STATE_RESTRICTS =
      new ImmutableMultimap.Builder<SessionState, String>()
          .put(SessionState.PREREGISTRATION, "USER")
          .put(SessionState.PREREGISTRATION, "NICK").build();

  private final Users users;
  private final ChannelService channelService;

  public CommandFactory(Users users, ChannelService channelService) {
    this.users = users;
    this.channelService = channelService;
  }

  public AbstractCommand newCommand(SessionState state, ParsedMessage command) {
    String verb = command.getVerb();
    if (STATE_RESTRICTS.containsKey(state) && !STATE_RESTRICTS.get(state).contains(verb)) {
      return new Unknown();
    }

    switch (verb) {
      case "AWAY":
        return new Away();
      case "FLOOD":
        return new Flood();
      case "JOIN":
        return new Join(channelService);
      case "LIST":
        return new List(channelService);
      case "LUSERS":
        return new Lusers(users, channelService);
      case "MODE":
        return new Mode(channelService);
      case "NAMES":
        return new Names();
      case "NICK":
        return new Nick(users);
      case "PART":
        return new Part(channelService);
      case "PING":
        return new Ping();
      case "PRIVMSG":
        return new Privmsg(users, channelService);
      case "QUIT":
        return new Quit();
      case "TOPIC":
        return new Topic(channelService);
      case "USER":
        return new User();
      case "WHO":
        return new Who(channelService);
      case "WHOIS":
        return new Whois(users, channelService);
      case "WHOWAS":
        return new Whowas();
      default:
        return new Unknown();
    }
  }
}
