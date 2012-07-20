package squirrels.ircd;

import org.junit.Assert;
import org.junit.Test;

public class ChannelsTest {
  @Test
  public void validNames() {
    String[] valid = {"#not-world", "#channel"};
    for (String chan : valid) {
      Assert.assertTrue("Channel " + chan + " is valid", Channels.isValid(chan));
    }
  }

  @Test
  public void invalidNames() {
    String[] invalid = {"#", "hello", "# ", "#,", "#" + 0x07};
    for (String chan : invalid) {
      Assert.assertFalse("Channel " + chan + " is not valid", Channels.isValid(chan));
    }
  }
}
