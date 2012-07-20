package squirrels.ircd;

import com.google.common.base.Preconditions;

/**
 * A TokenBucket is a rate limiter modeled after a bucket containing some maximum number of tokens
 * which are removed at arbitrary intervals and replenished at a predictable interval.
 *
 * Instances of this class may be safely used from multiple threads.
 */
public class TokenBucket {
  private final int maxCapacity;
  private final int replenishMs;
  private final Clock clock;

  private int currentCapacity;
  private long lastFilled;

  /**
   * Constructor.
   *
   * @param maxCapacity      The maximum number of tokens allowed in the bucket. This is also the
   *                         initial quantity. May be 0.
   * @param refillIntervalMs The interval (in milliseconds) at which a single token should be added
   *                         to the bucket. Must be a positive integer.
   */
  public TokenBucket(int maxCapacity, int refillIntervalMs) {
    this(maxCapacity, refillIntervalMs, new SystemClock());
  }

  /**
   * Constructor.
   *
   * @param maxCapacity      The maximum number of tokens allowed in the bucket. This is also the
   *                         initial quantity. May be 0.
   * @param refillIntervalMs The interval (in milliseconds) at which a single token should be added
   *                         to the bucket. Must be a positive integer.
   * @param clock            The Clock implementation to use.
   */
  public TokenBucket(int maxCapacity, int refillIntervalMs, Clock clock) {
    Preconditions.checkArgument(refillIntervalMs > 0, "refillIntervalMs > 0");
    this.clock = clock;
    this.maxCapacity = maxCapacity;
    this.currentCapacity = maxCapacity;
    this.replenishMs = refillIntervalMs;
    this.lastFilled = clock.now();
  }

  /**
   * Returns true if there are sufficient tokens available, and false otherwise.
   */
  public synchronized boolean consume() {
    long now = clock.now();
    int replenishAmount = (int) (now - lastFilled) / replenishMs;
    if (replenishAmount > 0) {
      currentCapacity = Math.min(maxCapacity, currentCapacity + replenishAmount);
      lastFilled = now;
    }
    if (currentCapacity > 0) {
      currentCapacity -= 1;
      return true;
    } else {
      return false;
    }
  }

  /**
   * Puts the thread to sleep until a token is available.
   */
  public void await() throws InterruptedException {
    while (!consume()) {
      Thread.sleep(replenishMs, 100);
    }
  }

  @Override
  public String toString() {
    return "TokenBucket{" +
        "maxCapacity=" + maxCapacity +
        ", replenishMs=" + replenishMs +
        ", currentCapacity=" + currentCapacity +
        ", lastFilled=" + lastFilled +
        '}';
  }
}
