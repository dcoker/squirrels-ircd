package squirrels.ircd.commands;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import squirrels.ircd.Channel;
import squirrels.ircd.ChannelService;
import squirrels.ircd.Session;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.ParsedMessage;

import java.util.Arrays;

public class List extends AbstractCommand {
  private final ChannelService channelService;

  List(ChannelService channelService) {
    this.channelService = channelService;
  }

  @Override
  public void execute(ParsedMessage command, Session session) {
    Iterable<Channel> chanList = channelService.list();
    session.response(Code.RPL_LISTSTART).send();

    Predicate<Channel> include = Predicates.alwaysTrue();
    if (command.getParameters() != null) {
      String[] split = command.getParameters()[0].split(",");
      include = Predicates.compose(Predicates.in(Arrays.asList(split)), Channel.toNameFn());
    }

    for (Channel channel : Iterables.filter(chanList, include)) {
      session.response(Code.RPL_LIST)
          .add(channel.getName())
          .add(channel.getCount())
          .add(Strings.nullToEmpty(channel.getTopic())).send();
    }
    session.response(Code.RPL_LISTEND).send();
  }
}
