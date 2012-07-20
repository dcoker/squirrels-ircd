package squirrels.ircd.commands;

import com.google.common.base.Strings;
import squirrels.ircd.Session;
import squirrels.ircd.messages.ParsedMessage;

public class User extends AbstractCommand {
  @Override
  public void execute(ParsedMessage command, Session session) {
    if (command.getParameters().length < 3) {
      // silently fail
      return;
    }
    session.setUsername(command.getParameters()[0]);
    session.setRemoteHost(command.getParameters()[1]);
    session.setServerName(command.getParameters()[2]);
    session.setRealName(Strings.nullToEmpty(command.getPayload()));
  }
}
