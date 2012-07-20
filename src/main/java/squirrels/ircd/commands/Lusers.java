package squirrels.ircd.commands;

import squirrels.ircd.ChannelService;
import squirrels.ircd.Session;
import squirrels.ircd.Users;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.ParsedMessage;

public class Lusers extends AbstractCommand {
  private final Users users;
  private final ChannelService channels;

  Lusers(Users users, ChannelService channels) {
    this.users = users;
    this.channels = channels;
  }

  @Override
  public void execute(ParsedMessage command, Session session) {
    int channelCount = channels.list().size();
    int userCount = users.size();
    int invisibleUserCount = 0;
    int serverCount = 0;
    int ops = 0;
    int serverClients = 0;
    int highestConnectionCount = users.getHighestUserCount();

    session.response(Code.RPL_LUSERCLIENT)
        .setPayload("There are %d users and %d invisible on %d servers", userCount,
            invisibleUserCount, serverCount)
        .send();
    session.response(Code.RPL_LUSEROP)
        .add(ops)
        .send();
    session.response(Code.RPL_LUSERUNKNOWN)
        .add(0)
        .send();
    session.response(Code.RPL_LUSERCHANNELS)
        .add(channelCount)
        .send();
    session.response(Code.RPL_LUSERME)
        .setPayload("I have %d clients and %d servers", userCount, serverClients)
        .send();
    session.response(Code.X_USERCOUNT)
        .add(userCount)
        .add(userCount)
        .setPayload("Current local users %d, max %d", userCount, highestConnectionCount).send();
    session.response(Code.X_GLOBALCOUNT)
        .add(userCount)
        .add(userCount)
        .setPayload("Current global users %d, max %d", userCount, highestConnectionCount).send();
    session.response(Code.RPL_STATSDLINE)
        .setPayload("Highest connection count: %d (%d clients)", highestConnectionCount,
            highestConnectionCount)
        .send();
  }
}
