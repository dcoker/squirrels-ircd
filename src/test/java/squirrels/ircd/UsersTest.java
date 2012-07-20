package squirrels.ircd;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UsersTest {

  @Test
  public void normalize() {
    assertEquals("eap", Users.normalize("eap"));
    assertEquals("eap{", Users.normalize("eap["));
    assertEquals("eap|", Users.normalize("eap\\"));
    assertEquals("{friend}", Users.normalize("[FRIEND]"));
  }
}
