package squirrels.ircd.commands;

import com.google.common.base.Strings;
import squirrels.ircd.Session;
import squirrels.ircd.messages.ParsedMessage;

import java.util.logging.Logger;

public class Flood extends AbstractCommand {
  private static final Logger logger = Logger.getLogger(Flood.class.getCanonicalName());
  private static final int MESSAGE_SIZE = 1024567;
  private static final String REQUIRED_USER_MODE = "O";
  public static final int NUM_MESSAGES = 10;

  @Override
  public void execute(ParsedMessage command, Session session) {
    if (session.getMode().contains(REQUIRED_USER_MODE)) {
      logger.info("Flooding " + session);
      for (char i = 'A'; i < 'A' + NUM_MESSAGES; i++) {
        session.receive(session.getIdentity(), "PRIVMSG")
            .add(session.getNick())
            .setPayload("{" + Strings.repeat(Character.toString(i), MESSAGE_SIZE - 2) + "}")
            .send();
      }
    } else {
      session.receive(session.getIdentity(), "PRIVMSG")
          .add(session.getNick())
          .setPayload("You do not have a license.")
          .send();
    }
  }
}
