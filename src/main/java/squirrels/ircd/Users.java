package squirrels.ircd;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.regex.Pattern;

public class Users implements NickChangeListener, DisconnectListener {
  private final Map<String, SoftReference<Session>> sessions = Maps.newConcurrentMap();
  private int highestUserCount;

  public int size() {
    int numUsers = sessions.size();
    maybeUpdateHighUserCount(numUsers);
    return numUsers;
  }

  private void maybeUpdateHighUserCount(int numUsers) {
    highestUserCount = Math.max(highestUserCount, numUsers);
  }

  public int getHighestUserCount() {
    maybeUpdateHighUserCount(sessions.size());
    return highestUserCount;
  }

  @Override
  public void nickChange(Session session, String oldIdentity, String oldNick) {
    if (oldNick != null) {
      SoftReference<Session> oldSlot = sessions.remove(oldNick);
      sessions.put(normalize(session.getNick()), oldSlot);
    } else {
      sessions.put(normalize(session.getNick()), new SoftReference<>(session));
    }
  }

  @Override
  public void disconnect(Session session) {
    if (session.getNick() != null) {  // may be null pre-registration
      sessions.remove(normalize(session.getNick()));
    }
  }

  public Session getSession(String nick) {
    SoftReference<Session> sessionSoftReference = sessions.get(normalize(nick));
    if (sessionSoftReference == null) {
      return null;
    }
    return sessionSoftReference.get();
  }

  @VisibleForTesting
  static String normalize(String nick) {
    return nick.toLowerCase()
        .replaceAll(Pattern.quote("["), "{")
        .replaceAll(Pattern.quote("]"), "}")
        .replaceAll(Pattern.quote("\\"), "|")
        .replaceAll(Pattern.quote("-"), "_")
        .replaceAll(Pattern.quote("~"), "^");
  }
}
