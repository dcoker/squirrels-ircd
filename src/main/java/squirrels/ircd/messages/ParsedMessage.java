package squirrels.ircd.messages;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

public final class ParsedMessage {
  private static final String SPACE = " ";

  private String prefix;
  private String verb;
  private String[] parameters;
  private String payload;

  private ParsedMessage() {

  }

  public String getVerb() {
    return verb;
  }

  public String[] getParameters() {
    if (parameters != null) {
      return Arrays.copyOf(parameters, parameters.length);
    } else {
      return null;
    }
  }

  public String getPayload() {
    return payload;
  }

  @VisibleForTesting
  ParsedMessage(String prefix, String verb, String[] parameters, String payload) {
    this.prefix = prefix;
    this.verb = verb;
    this.parameters = parameters;
    this.payload = payload;
  }

  public static ParsedMessage parse(String line) {
    ParsedMessage command = new ParsedMessage();

    boolean first = true;
    boolean hasVerb = false;
    boolean hasPayload = false;

    List<String> args = Lists.newLinkedList();
    List<String> payload = Lists.newLinkedList();
    for (String piece : line.split(SPACE)) {
      if (first && piece.startsWith(":")) {
        command.prefix = piece.substring(1);
      } else if (!hasVerb) {
        command.verb = piece;
        hasVerb = true;
      } else if (!hasPayload) {
        if (piece.startsWith(":")) {
          hasPayload = true;
          payload.add(piece.substring(1));
        } else {
          args.add(piece);
        }
      } else {
        payload.add(piece);
      }

      first = false;
    }

    if (!args.isEmpty()) {
      command.parameters = args.toArray(new String[args.size()]);
    }
    if (!payload.isEmpty()) {
      command.payload = Joiner.on(" ").join(payload);
    }

    command.normalize();
    return command;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ParsedMessage)) {
      return false;
    }
    ParsedMessage other = (ParsedMessage) obj;
    return Objects.equal(other.prefix, prefix) &&
        Objects.equal(other.verb, verb) &&
        Arrays.equals(other.parameters, parameters) &&
        Objects.equal(other.payload, payload);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(prefix, verb, parameters, payload);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("prefix", prefix)
        .add("verb", verb)
        .add("parameters", parameters != null ? Joiner.on(",").join(parameters) : "null")
        .add("payload", payload).toString();
  }

  private void normalize() {
    if (verb != null) {
      verb = verb.toUpperCase();
    }
  }
}
