package squirrels.ircd.commands;

import squirrels.ircd.Session;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.ParsedMessage;

import java.util.logging.Logger;

public class Unknown extends AbstractCommand {
  private static final Logger logger = Logger.getLogger(Unknown.class.getCanonicalName());

  @Override
  public void execute(ParsedMessage command, Session session) {
    logger.info("UNKNOWN command: " + command);
    String verb = command.getVerb();
    if (verb == null) {
      verb = "(missing)";
    }
    session.response(Code.ERR_UNKNOWNCOMMAND).add(verb).send();
  }
}
