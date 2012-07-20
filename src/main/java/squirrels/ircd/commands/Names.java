package squirrels.ircd.commands;

import com.google.common.base.Strings;
import squirrels.ircd.Session;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.ParsedMessage;

public class Names extends AbstractCommand {

  @Override
  public void execute(ParsedMessage parsed, Session session) {
    // TODO
  }

  public void names(Session joiner, String channelName, String names) {
    String nameCode = "=";  // @ for secret channels, * for private, = for others (public)
    if (!Strings.isNullOrEmpty(names)) {
      joiner.response(Code.RPL_NAMREPLY).add(nameCode).add(channelName).setPayload(names).send();
    }
    joiner.response(Code.RPL_ENDOFNAMES).add(channelName).send();
  }
}
