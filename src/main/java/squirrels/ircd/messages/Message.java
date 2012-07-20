package squirrels.ircd.messages;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import squirrels.ircd.Session;

import java.util.List;

public class Message {

  private String prefix;
  private Integer code;
  private String targetNick;
  private final List<String> args = Lists.newLinkedList();
  private String payload;
  private final Session session;

  public Message() {
    session = null;
  }

  public Message(Session session) {
    this.session = session;
  }

  public Message(Session session, Code code) {
    this.session = session;
    setCode(code, session.getNick());
  }

  public void send() {
    session.write(this);
  }

  public Message preregistration() {
    prefix = null;
    return this;
  }

  public Message setPrefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  public final Message setCode(Code code, String target) {
    this.code = code.getNumeric();
    this.targetNick = target;
    if (code.getDefaultPayload() != null) {
      this.setPayload(code.getDefaultPayload());
    }
    return this;
  }

  public Message add(String param) {
    this.args.add(param);
    return this;
  }

  public Message add(long param) {
    this.args.add(String.valueOf(param));
    return this;
  }

  public Message setPayload(String payload) {
    this.payload = payload;
    return this;
  }

  public Message setPayload(String payload, Object... args) {
    this.payload = String.format(payload, args);
    return this;
  }

  public String build() {
    StringBuilder output = new StringBuilder();
    if (prefix != null) {
      output.append(":").append(prefix).append(" ");
    }
    if (code != null) {
      output.append(String.format("%03d", code)).append(" ");
    }
    if (targetNick != null) {
      output.append(targetNick).append(" ");
    }
    if (!args.isEmpty()) {
      output.append(Joiner.on(" ").join(args)).append(" ");
    }
    if (payload != null) {
      output.append(":").append(payload);
    }
    return output.toString();
  }
}
