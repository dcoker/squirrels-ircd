package squirrels.ircd.commands;

import squirrels.ircd.Channel;
import squirrels.ircd.ChannelService;
import squirrels.ircd.Session;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.ParsedMessage;

public class Topic extends AbstractCommand {
  public static final int MAX_TOPIC_LENGTH = 140;
  private final ChannelService channelService;

  Topic(ChannelService channelService) {
    this.channelService = channelService;
  }

  @Override
  public void execute(ParsedMessage command, Session session) {
    if (command.getParameters() == null) {
      session.response(Code.ERR_NEEDMOREPARAMS).add(command.getVerb()).send();
    } else if (command.getParameters().length == 1) {
      if (command.getPayload() != null) {
        topicChange(channelService, session, command);
      } else {
        topicInquiry(channelService, session, command);
      }
    }
  }

  private void topicInquiry(ChannelService channelService, Session session, ParsedMessage command) {
    String requestedChannel = command.getParameters()[0];
    Channel channel = channelService.get(requestedChannel);
    String topic = channel.getTopic();
    if (topic == null) {
      session.response(Code.RPL_NOTOPIC).add(channel.getName()).send();
    } else {
      session.response(Code.RPL_NOTOPIC).add(channel.getName()).setPayload(topic).send();
    }
  }

  private void topicChange(ChannelService channelService, Session session, ParsedMessage command) {
    String chanName = command.getParameters()[0];
    Channel channel = channelService.get(chanName);
    String newTopic = command.getPayload();
    if (channel == null) {
      session.response(Code.ERR_NOSUCHCHANNEL).add(chanName).send();
    } else {
      if (channel.contains(session)) {
        channel.setTopic(session, newTopic);
      } else {
        session.response(Code.ERR_NOTONCHANNEL).add(chanName).send();
      }
    }
  }
}
