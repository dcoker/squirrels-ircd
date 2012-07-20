package squirrels.ircd.messages;

import org.junit.Assert;
import org.junit.Test;

public class MessageTest {
  @Test
  public void preregistrationNotice() {
    Message builder = new Message();
    builder.preregistration();
    builder.add("NOTICE AUTH");
    builder.setPayload("*** Processing connection to irc.choopa.net");
    Assert.assertEquals("NOTICE AUTH :*** Processing connection to irc.choopa.net",
        builder.build());
  }

  @Test
  public void preregistrationUser() {
    Message builder = new Message();
    builder.preregistration().add("PING").setPayload("59817EA2");
    Assert.assertEquals("PING :59817EA2", builder.build());
  }

  @Test
  public void one() {
    Message builder = new Message().setPrefix("prefix");
    builder.setCode(Code.RPL_YOURHOST, "meowmeow0").setPayload("Your host is blah");
    Assert.assertEquals(":prefix 002 meowmeow0 :Your host is blah", builder.build());
  }

  @Test
  public void fyi() {
    Message builder = new Message().setPrefix("prefix");
    builder.setCode(Code.X_USERCOUNT, "meowmeow0").add(1305).add(1358).setPayload
        ("Current local users 1305, max 1358");
    Assert.assertEquals(":prefix 265 meowmeow0 1305 1358 :Current local users 1305, " +
        "max 1358", builder.build());
  }

  @Test
  public void unknownCommand() {
    Message builder = new Message().setPrefix("prefix");
    builder.setCode(Code.ERR_UNKNOWNCOMMAND, "hellochoo")
        .setPayload("Unknown command").add("/snarfle");
    Assert.assertEquals(":prefix 421 hellochoo /snarfle :Unknown command",
        builder.build());
  }

  @Test
  public void modeResponse() {
    String nick = "dcoker";
    Assert.assertEquals(":dcoker MODE dcoker :+i", new Message().setPrefix(nick).add
        ("MODE").add(nick).setPayload("+i").build());
  }
}
