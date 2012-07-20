package squirrels.ircd.commands;

import squirrels.ircd.Channel;
import squirrels.ircd.ChannelService;
import squirrels.ircd.Channels;
import squirrels.ircd.Session;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.ParsedMessage;

public class Mode extends AbstractCommand {
  private final ChannelService channelService;

  Mode(ChannelService channelService) {
    this.channelService = channelService;
  }

  @Override
  public void execute(ParsedMessage command, Session session) {
    String target = command.getParameters()[0];
    if (Channels.isChannel(target)) {
      channelMode(command, channelService, session);
    } else {
      userMode(command, session, target);
    }
  }

  private void userMode(ParsedMessage command, Session session, String target) {
    if (!target.equals(session.getNick())) {
      session.response(Code.ERR_USERSDONTMATCH).send();
      return;
    }

    if (command.getParameters().length == 1) {
      session.response(Code.RPL_UMODEIS).add(session.getMode()).send();
    } else if (command.getParameters().length == 2) {
      String mode = command.getParameters()[1];
      session.adjustMode(mode);
      session.confirm("MODE").add(session.getNick()).setPayload(mode).send();
    } else {
      session.response(Code.ERR_NEEDMOREPARAMS).add(command.getVerb()).send();
    }
  }

  private void channelMode(ParsedMessage command, ChannelService channelService, Session session) {
    String object = command.getParameters()[0];

    Channel channel = channelService.get(object);
    if (channel == null) {
      session.response(Code.ERR_NOSUCHCHANNEL).add(object).send();
    } else {
      if (command.getParameters().length == 1) {
        session.response(Code.RPL_CHANNELMODEIS).add(object).add("+sn").send();
        session.response(Code.X_CHANNEL_CREATED_TIME)
            .add(object)
            .add(channel.getCreatedSeconds())
            .send();
      } else {
        // mode change, unimplemented
      }
    }
  }
}
