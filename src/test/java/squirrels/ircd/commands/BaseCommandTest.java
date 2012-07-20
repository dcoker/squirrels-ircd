package squirrels.ircd.commands;

import com.google.common.base.Charsets;
import org.junit.Before;
import squirrels.ircd.ChannelService;
import squirrels.ircd.ChannelServiceImpl;
import squirrels.ircd.Session;
import squirrels.ircd.Users;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class BaseCommandTest {
  protected Users users;
  protected ChannelService channels;
  protected Runnable wakeupFn;
  protected Runnable wakeupFn2;
  protected AtomicInteger wakeupCount;
  protected AtomicInteger wakeupCount2;
  protected Session session;
  protected Session session2;

  @Before
  public void before() {
    users = new Users();
    channels = new ChannelServiceImpl();
    wakeupCount = new AtomicInteger();
    wakeupCount2 = new AtomicInteger();
    wakeupFn = new Waker(wakeupCount);
    wakeupFn2 = new Waker(wakeupCount2);

    session = new Session("remotehost:port", wakeupFn, "realservername");
    session.setNick("usernick");
    session.setRemoteHost("server");

    session2 = new Session("remotehost2:port2", wakeupFn2, "realservername");
    session2.setNick("usernick2");
    session2.setRemoteHost("sever");
  }

  void reset() {
    wakeupCount.set(0);
    session.getWrites().clear();
  }

  void assertResponses(Session session, String... responses) {
    ConcurrentLinkedQueue<ByteBuffer> written = session.getWrites();
    assertEquals(responses.length, written.size());
    for (int i = 0; i < responses.length; i++) {
      assertEquals(responses[i], new String(written.poll().array(), Charsets.UTF_8).trim());
    }
  }

  private class Waker implements Runnable {

    private final AtomicInteger waker;

    private Waker(AtomicInteger counter) {
      waker = counter;
    }

    @Override
    public void run() {
      waker.incrementAndGet();
    }
  }
}
