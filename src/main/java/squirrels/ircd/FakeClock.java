package squirrels.ircd;

public class FakeClock implements Clock {
  private long nowMs = System.currentTimeMillis();

  public FakeClock(int i) {
    this.nowMs = i;
  }

  public long forward(long delta) {
    nowMs += delta;
    return nowMs;
  }

  @Override
  public long now() {
    return nowMs;
  }
}
