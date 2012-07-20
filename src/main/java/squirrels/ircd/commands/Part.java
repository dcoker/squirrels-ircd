package squirrels.ircd.commands;

import squirrels.ircd.Channel;
import squirrels.ircd.ChannelService;
import squirrels.ircd.Session;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.ParsedMessage;

public class Part extends AbstractCommand {

  private final ChannelService channelService;

  Part(ChannelService channelService) {
    this.channelService = channelService;
  }

  @Override
  public void execute(ParsedMessage command, Session session) {
    if (command.getParameters() == null) {
      session.response(Code.ERR_NEEDMOREPARAMS).add(command.getVerb()).send();
      return;
    }

    String chanNames = command.getParameters()[0];
    for (String chanName : chanNames.split(",")) {
      Channel channel = channelService.get(chanName);
      if (channel == null) {
        session.response(Code.ERR_NOSUCHCHANNEL).add(chanName).send();
        continue;
      }

      if (!channel.part(session)) {
        session.response(Code.ERR_NOTONCHANNEL).add(chanName).send();
      }
    }
  }
}
