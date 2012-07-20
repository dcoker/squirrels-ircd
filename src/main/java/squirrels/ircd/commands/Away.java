package squirrels.ircd.commands;

import squirrels.ircd.Session;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.ParsedMessage;

public class Away extends AbstractCommand {

  @Override
  public void execute(ParsedMessage parsed, Session session) {
    if (parsed.getPayload() != null) {
      session.adjustMode("+a");
      session.setAwayMessage(parsed.getPayload());
      session.response(Code.RPL_NOWAWAY).setPayload(parsed.getPayload()).send();
    } else {
      session.adjustMode("-a");
      session.setAwayMessage(null);
      session.response(Code.RPL_UNAWAY).send();
    }
  }
}
