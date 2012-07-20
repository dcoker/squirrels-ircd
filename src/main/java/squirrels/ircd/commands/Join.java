package squirrels.ircd.commands;

import squirrels.ircd.Channel;
import squirrels.ircd.ChannelService;
import squirrels.ircd.Channels;
import squirrels.ircd.Session;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.ParsedMessage;

public class Join extends AbstractCommand {

  private final ChannelService channelService;

  Join(ChannelService channelService) {
    this.channelService = channelService;
  }

  @Override
  public void execute(ParsedMessage command, Session session) {
    String chanList;
    if (command.getPayload() != null) {
      chanList = command.getPayload();
    } else if (command.getParameters() != null && command.getParameters().length == 1) {
      chanList = command.getParameters()[0];
    } else {
      session.response(Code.ERR_NEEDMOREPARAMS).add(command.getVerb()).send();
      return;
    }

    for (String chanName : chanList.split(",")) {
      if (!Channels.isValid(chanName)) {
        session.response(Code.ERR_NOSUCHCHANNEL).add(chanName).send();
        continue;
      }
      Channel channel = channelService.getOrCreate(chanName);
      channel.join(session);
    }
  }
}

