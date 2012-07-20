package squirrels.ircd;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public final class Channels {

  public static final int MAX_CHANNEL_LEN = 50;

  private Channels() {
  }

  public static boolean isChannel(String target) {
    return target.startsWith("#") || target.startsWith("&");
  }

  @VisibleForTesting
  public static boolean isValid(String chan) {
    Predicate<Character> valid = Predicates.and(CharMatcher.ASCII, CharMatcher.noneOf(" ," + 0x07));
    return isChannel(chan)
        && chan.length() >= 2
        && chan.length() <= MAX_CHANNEL_LEN
        && CharMatcher.forPredicate(valid).matchesAllOf(chan);
  }
}
