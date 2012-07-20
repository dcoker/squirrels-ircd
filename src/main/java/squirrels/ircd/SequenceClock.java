package squirrels.ircd;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Deque;

public class SequenceClock implements Clock {
  private final Deque<Long> nows;

  public SequenceClock(Long... times) {
    this.nows = Lists.newLinkedList(Arrays.asList(times));
  }

  @Override
  public long now() {
    return nows.pop();
  }

  public int remaining() {
    return nows.size();
  }
}
