package squirrels.ircd.commands;

import squirrels.ircd.Session;
import squirrels.ircd.messages.ParsedMessage;

public class Quit extends AbstractCommand {

  @Override
  public void execute(ParsedMessage parsed, Session session) {
    session.close();
  }
}
