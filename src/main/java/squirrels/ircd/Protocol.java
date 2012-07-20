package squirrels.ircd;

import squirrels.ircd.commands.Command;
import squirrels.ircd.commands.CommandFactory;
import squirrels.ircd.commands.Nick;
import squirrels.ircd.commands.Topic;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.Message;
import squirrels.ircd.messages.ParsedMessage;

import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

class Protocol {
  private static final Logger logger = Logger.getLogger(Protocol.class.getCanonicalName());

  private final CommandFactory factory;
  private final String networkName;

  public Protocol(ChannelService channelService, Users users, String networkName) {
    this.networkName = networkName;
    this.factory = new CommandFactory(users, channelService);
  }

  void hello(Session session) {
    session.write(new Message().preregistration()
        .add("NOTICE AUTH")
        .setPayload("*** Processing connection {-}==="));
  }

  void process(Session session, String message) {
    ParsedMessage parsed = ParsedMessage.parse(message);
    Command command = factory.newCommand(session.getState(), parsed);
    logger.fine("Parsed: " + parsed);
    if (command.isHuman()) {
      session.updateActivityTimestamp();
    }
    command.execute(parsed, session);

    // Some clients send NICK and USER in different orders, but we need both to fully register
    // the user.
    if (session.getState() == SessionState.PREREGISTRATION
        && session.getServerName() != null
        && session.getNick() != null) {
      session.setState(SessionState.REGISTERED);
      sendWelcome(session);
    }
  }

  private void sendWelcome(Session session) {
    String version = "0000";
    String userModes = "abcdefghijklmnopqrstuvwxyz";
    String chanModes = "abcdefghijklmnopqrstuvwxyz";
    long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

    session.response(Code.RPL_WELCOME)
        .setPayload("Welcome to Internet Relay Network " + session.getNick())
        .send();
    session.response(Code.RPL_YOURHOST)
        .setPayload("Your host is " + session.getActualServerName() + " running version " +
            version + ".")
        .send();
    session.response(Code.RPL_CREATED)
        .setPayload("This server has been running for " + uptime + "ms")
        .send();
    session.response(Code.RPL_MYINFO)
        .setPayload("%s %s %s %s", session.getServerName(), version, userModes, chanModes)
        .send();
    session.response(Code.RPL_ISUPPORT)
        .setPayload("CHANLIMIT=#:%d NICKLEN=%d NETWORK=%s CASEMAPPING=strict-rfc1459 " +
            "TOPICLEN=%d CHANNELLEN=%d CHARSET=utf-8",
            Integer.MAX_VALUE,
            Nick.MAX_NICK_LENGTH,
            networkName,
            Topic.MAX_TOPIC_LENGTH,
            Channels.MAX_CHANNEL_LEN)
        .send();
    session.response(Code.RPL_MOTDSTART)
        .send();
    session.response(Code.RPL_MOTD).setPayload("-=-=-=-{{ meow }}-=-=-=-")
        .send();
    session.response(Code.RPL_ENDOFMOTD)
        .send();
    session.setState(SessionState.REGISTERED);
  }
}
