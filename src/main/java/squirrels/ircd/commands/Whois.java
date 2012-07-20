package squirrels.ircd.commands;

import com.google.common.base.Joiner;
import squirrels.ircd.ChannelService;
import squirrels.ircd.Session;
import squirrels.ircd.Users;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.ParsedMessage;

import java.util.concurrent.TimeUnit;

public class Whois extends AbstractCommand {
  private final Users users;
  private final ChannelService channelService;

  Whois(Users users, ChannelService channelService) {
    this.users = users;
    this.channelService = channelService;
  }

  @Override
  public void execute(ParsedMessage command, Session session) {
    if (command.getParameters() == null) {
      return;
    }
    String target = command.getParameters()[0];
    Session targetSession = users.getSession(target);
    if (targetSession == null) {
      session.response(Code.ERR_NOSUCHNICK).add(target).send();
      return;
    }
    session.response(Code.RPL_WHOISUSER)
        .add(targetSession.getNick())
        .add(targetSession.getUsername())
        .add(targetSession.getActualRemoteHost())
        .add("*")
        .setPayload(targetSession.getRealname())
        .send();
    session.response(Code.RPL_WHOISCHANNELS)
        .add(targetSession.getNick())
        .setPayload(Joiner.on(" ").join(channelService.locate(targetSession)))
        .send();
    session.response(Code.RPL_WHOISSERVER)
        .add(targetSession.getNick())
        .add(targetSession.getActualServerName())
        .send();
    session.response(Code.X_ACTUALLY_USING_HOST)
        .add(targetSession.getNick())
        .add(targetSession.getActualRemoteHost())
        .send();
    session.response(Code.RPL_WHOISIDLE)
        .add(targetSession.getNick())
        .add(TimeUnit.SECONDS.convert(
            System.currentTimeMillis() - targetSession.getLastActivityMs(), TimeUnit.MILLISECONDS))
        .add(TimeUnit.SECONDS.convert(targetSession.getLoginTimeMs(), TimeUnit.MILLISECONDS))
        .send();
    session.response(Code.RPL_ENDOFWHOIS)
        .add(targetSession.getNick())
        .send();
  }
}
