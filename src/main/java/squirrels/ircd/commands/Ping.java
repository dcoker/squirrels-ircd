package squirrels.ircd.commands;

import squirrels.ircd.Session;
import squirrels.ircd.messages.Message;
import squirrels.ircd.messages.ParsedMessage;

public class Ping extends AbstractCommand {

  @Override
  public boolean isHuman() {
    return false;
  }

  @Override
  public void execute(ParsedMessage command, Session session) {
    if (command.getPayload() != null) {
      session.write(new Message()
          .preregistration()
          .add("PONG")
          .setPayload(command.getPayload()));
    } else if (command.getParameters() != null) {
      session.write(new Message()
          .setPrefix(command.getParameters()[0])
          .add("PONG")
          .add(command.getParameters()[0])
          .setPayload(command.getParameters()[0]));
    }
  }
}
