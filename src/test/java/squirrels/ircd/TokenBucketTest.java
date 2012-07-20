package squirrels.ircd;

import com.google.common.base.Stopwatch;
import org.junit.Test;

import static org.junit.Assert.*;

public class TokenBucketTest {

  private static final int EPSILON = 2;

  @Test
  public void consumeThroughput() throws InterruptedException {
    int testDurationMs = 3000;
    int refreshMs = 100;
    TokenBucket lb = new TokenBucket(1, refreshMs);
    Stopwatch realTime = new Stopwatch().start();
    long allowed = 0;
    while (realTime.elapsedMillis() < testDurationMs) {
      if (lb.consume()) {
        allowed++;
      }
      Thread.sleep(10);
    }
    int expected = testDurationMs / refreshMs;
    assertTrue((expected - EPSILON) < allowed && allowed < (expected + EPSILON));
  }

  @Test
  public void awaitThroughput() throws InterruptedException {
    int testDurationMs = 3000;
    int refreshMs = 100;
    TokenBucket lb = new TokenBucket(1, refreshMs);
    Stopwatch realTime = new Stopwatch().start();
    long allowed = 0;
    while (realTime.elapsedMillis() < testDurationMs) {
      lb.await();
      allowed++;
    }
    int expected = testDurationMs / refreshMs;
    assertTrue((expected - EPSILON) < allowed && allowed < (expected + EPSILON));
  }

  @Test
  public void await() throws InterruptedException {
    SequenceClock clock = new SequenceClock(0L, 100L, 200L, 300L, 999L, 1000L);
    TokenBucket lb = new TokenBucket(1, 1000, clock);
    lb.await();
    assertTrue(true);
    lb.await();
    assertEquals(0, clock.remaining());
  }

  @Test
  public void zero() {
    FakeClock clock = new FakeClock(0);
    TokenBucket lb = new TokenBucket(0, 1000, clock);
    assertFalse(lb.consume());
    clock.forward(1000);
    assertFalse(lb.consume());
  }

  @Test
  public void one() throws InterruptedException {
    FakeClock clock = new FakeClock(0);
    TokenBucket lb = new TokenBucket(1, 1000, clock);
    assertTrue(lb.consume());
    assertFalse(lb.consume());
    assertFalse(lb.consume());
    clock.forward(800);
    assertFalse(lb.consume());
    clock.forward(199);
    assertFalse(lb.consume());
    clock.forward(1);
    assertTrue(lb.consume());
  }

  @Test
  public void two() throws InterruptedException {
    FakeClock clock = new FakeClock(0);
    TokenBucket lb = new TokenBucket(2, 1000, clock);
    assertTrue(lb.consume());
    assertTrue(lb.consume());
    assertFalse(lb.consume());
    clock.forward(800);
    assertFalse(lb.consume());
    clock.forward(200);
    assertTrue(lb.consume());
    assertFalse(lb.consume());
    clock.forward(999);
    assertFalse(lb.consume());
    clock.forward(1);
    assertTrue(lb.consume());
  }
}
