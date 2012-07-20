package squirrels.ircd.commands;

import squirrels.ircd.Session;
import squirrels.ircd.messages.ParsedMessage;

public interface Command {
  public void execute(ParsedMessage parsed, Session session);
  public boolean isHuman();
}
