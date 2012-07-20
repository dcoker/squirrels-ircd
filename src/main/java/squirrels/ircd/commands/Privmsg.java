package squirrels.ircd.commands;

import squirrels.ircd.*;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.ParsedMessage;

public class Privmsg extends AbstractCommand {
  private final Users users;
  private final ChannelService channelService;

  Privmsg(Users users, ChannelService channelService) {
    this.users = users;
    this.channelService = channelService;
  }

  @Override
  public void execute(ParsedMessage command, Session session) {
    if (command.getParameters() == null) {
      session.response(Code.ERR_NEEDMOREPARAMS).add(command.getVerb()).send();
      return;
    }

    String[] split = command.getParameters()[0].split(",");
    for (String target : split) {
      if (Channels.isChannel(target)) {
        messageToChannel(command, channelService, session, target);
      } else {
        messageToUser(users, session, command, target);
      }
    }
  }

  private void messageToUser(Users users, Session session, ParsedMessage command,
                             String target) {
    Session targetSession = users.getSession(target);
    if (targetSession == null) {
      session.response(Code.ERR_NOSUCHNICK).add(target).send();
    } else {
      if (targetSession.getAwayMessage() != null) {
        session.response(Code.RPL_AWAY).add(targetSession.getNick()).setPayload(targetSession
            .getAwayMessage()).send();
      }

      targetSession.receive(session.getIdentity(), "PRIVMSG")
          .add(target)
          .setPayload(command.getPayload())
          .send();
    }
  }

  private void messageToChannel(ParsedMessage command, ChannelService channelService,
                                Session session, String target) {
    Channel channel = channelService.get(target);
    if (channel == null) {
      session.response(Code.ERR_NOSUCHCHANNEL).add(target).send();
    } else {
      channel.push(session, session.getNick(), command.getPayload());
    }
  }
}
