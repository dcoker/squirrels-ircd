package squirrels.ircd;

import java.util.Collection;
import java.util.List;

public interface ChannelService {

  public Channel get(String name);

  public Channel getOrCreate(String name);

  public List<Channel> list();

  public Collection<String> locate(Session session);

  void notifyPart(Session session, Channel channel);

  void notifyJoin(Session joiner, Channel channel);
}
