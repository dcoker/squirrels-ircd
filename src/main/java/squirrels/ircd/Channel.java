package squirrels.ircd;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import squirrels.ircd.commands.Names;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.Message;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Channel implements DisconnectListener, NickChangeListener {
  private final long created = System.currentTimeMillis();
  private final String name;
  private final ChannelService channelService;
  private String topic;
  private long topicLastSetTimestamp;
  private String topicLastChangedByNick;
  private final List<Session> members = Lists.newArrayList();

  public Channel(String name, ChannelService channelService) {
    this.name = name;
    this.channelService = channelService;
  }

  public boolean contains(Session session) {
    return members.contains(session);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("name", name).add("mems", members.size()).toString();
  }

  public int getCount() {
    return members.size();
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(Session setter, String topic) {
    this.topic = topic;
    this.topicLastChangedByNick = setter.getNick();
    this.topicLastSetTimestamp = System.currentTimeMillis();
    broadcast(new Message().setPrefix(setter.getIdentity()).add("TOPIC").add(name)
        .setPayload(topic));
  }

  public String getName() {
    return name;
  }

  public void join(Session joiner) {
    if (contains(joiner)) {
      return;
    }

    members.add(joiner);
    channelService.notifyJoin(joiner, this);
    joiner.notifyOnDisconnect(this);
    joiner.notifyOnNickChange(this);

    announceJoin(joiner);
    sendStateToJoiner(joiner);
  }

  private void announceJoin(Session joiner) {
    broadcast(joiner.confirm("JOIN").setPayload(name));
  }

  private void sendStateToJoiner(Session joiner) {
    if (topic != null) {
      joiner.response(Code.RPL_TOPIC).add(name).setPayload(topic).send();
      joiner.response(Code.X_TOPIC_LAST_CHANGED_BY)
          .add(name)
          .add(topicLastChangedByNick)
          .add(TimeUnit.SECONDS.convert(topicLastSetTimestamp, TimeUnit.MILLISECONDS))
          .send();
    }
    new Names().names(joiner, name,
        Joiner.on(" ").join(Iterables.transform(members, Session.toNickFn())));
  }

  @Override
  public void disconnect(Session session) {
    members.remove(session);
    channelService.notifyPart(session, this);
    push(session, new Message().setPrefix(session.getIdentity()).add("QUIT").add(name)
        .setPayload("disconnected"));
  }

  void broadcast(Message message) {
    push(null, message);
  }

  void push(Session session, Message message) {
    for (Session member : members) {
      if (session != null && member.getNick().equals(session.getNick())) {
        continue;
      }
      member.write(message);
    }
  }

  public void push(Session session, String origin, String message) {
    if (members.contains(session)) {
      push(session, new Message()
          .setPrefix(origin)
          .add("PRIVMSG")
          .add(name)
          .setPayload(message));
    } else {
      session.response(Code.ERR_CANNOTSENDTOCHAN).add(name).send();
    }
  }

  public List<Session> who() {
    return members;
  }

  public long getCreatedSeconds() {
    return TimeUnit.SECONDS.convert(created, TimeUnit.MILLISECONDS);
  }

  public boolean part(Session session) {
    if (!contains(session)) {
      return false;
    }
    broadcast(new Message()
        .setPrefix(session.getIdentity())
        .add("PART")
        .add(name));
    members.remove(session);
    channelService.notifyPart(session, this);
    return true;
  }

  @Override
  public void nickChange(Session session, String oldIdentity, String oldNick) {
    Message message = new Message()
        .setPrefix(oldIdentity)
        .add("NICK")
        .setPayload(session.getNick());
    push(session, message);
  }

  public static Function<Channel, String> toNameFn() {
    return new Function<Channel, String>() {
      @Override
      public String apply(Channel input) {
        return input.getName();
      }
    };
  }
}
