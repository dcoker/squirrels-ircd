package squirrels.ircd.commands;

import squirrels.ircd.Channel;
import squirrels.ircd.ChannelService;
import squirrels.ircd.Session;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.ParsedMessage;

public class Who extends AbstractCommand {
  private final ChannelService channelService;

  Who(ChannelService channelService) {
    this.channelService = channelService;
  }

  @Override
  public void execute(ParsedMessage command, Session session) {
    if (command.getParameters().length < 1) {
      session.response(Code.ERR_NEEDMOREPARAMS).add(command.getVerb()).send();
      return;
    }

    String chanName = command.getParameters()[0];
    execute(session, chanName, channelService.get(chanName));
  }

  void execute(Session session, String chanName, Channel channel) {
    if (channel == null) {
      session.response(Code.ERR_NOSUCHCHANNEL).add(chanName).send();
      return;
    }
    for (Session person : channel.who()) {
      session.response(Code.RPL_WHOREPLY)
          .add(channel.getName())
          .add(person.getUsername())
          .add(person.getActualRemoteHost())
          .add(person.getActualServerName())
          .add(person.getNick())
          .add("H" + person.getMode())
          .setPayload("0 " + person.getRealname())
          .send();
    }
    session.response(Code.RPL_ENDOFWHO).send();
  }
}
