package squirrels.ircd.commands;

import org.junit.Assert;
import org.junit.Test;

public class NickTest {
  @Test
  public void validNicknames() {
    String[] valid = {"a", "_a", "shrewm", "_eap", "ea_"};
    for (String nick : valid) {
      Assert.assertTrue("Nick " + nick + " should be valid", Nick.isValidNickname(nick));
    }
  }

  @Test
  public void invalidNicknames() {
    String[] invalid = {"0", "1", "-", "-a", "1a", "!hey!"};
    for (String nick : invalid) {
      Assert.assertFalse("Nick " + nick + " should not be valid", Nick.isValidNickname(nick));
    }
  }
}
