package squirrels.ircd;

import com.google.common.collect.*;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class ChannelServiceImpl implements ChannelService {
  private static final Logger logger = Logger.getLogger(
      ChannelServiceImpl.class.getCanonicalName());
  private final ConcurrentMap<String, Channel> channels = Maps.newConcurrentMap();
  private final Multimap<Session, Channel> phonebook = HashMultimap.create();

  @Override
  public Channel get(String name) {
    return channels.get(name);
  }

  public Collection<String> locate(Session session) {
    return ImmutableList.copyOf(Iterables.transform(phonebook.get(session),
        Channel.toNameFn()));
  }

  @Override
  public void notifyPart(Session session, Channel channel) {
    if (phonebook.remove(session, channel)) {
      maintenance(channel);
    }
  }

  @Override
  public void notifyJoin(Session session, Channel channel) {
    phonebook.put(session, channel);
  }

  @Override
  public Channel getOrCreate(String name) {
    Channel channel = new Channel(name, this);
    Channel tentative = channels.putIfAbsent(name, channel);
    if (tentative == null) {
      return channel;
    } else {
      return tentative;
    }
  }

  @Override
  public List<Channel> list() {
    return ImmutableList.copyOf(channels.values());
  }

  private void maintenance(Channel channel) {
    if (channel.getCount() == 0) {
      logger.info("Channel " + channel + " is now empty, removing.");
      channels.remove(channel.getName());
    }
  }
}
