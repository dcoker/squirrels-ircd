package squirrels.ircd;

public class SystemClock implements Clock {
  @Override
  public long now() {
    return System.currentTimeMillis();
  }
}
