package squirrels.ircd.commands;

import squirrels.ircd.Session;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.ParsedMessage;

public class Whowas extends AbstractCommand {

  @Override
  public void execute(ParsedMessage command, Session session) {
    if (command.getParameters() == null) {
      session.response(Code.ERR_NEEDMOREPARAMS).add(command.getVerb()).send();
    } else {
      for (String id : command.getParameters()[0].split(",")) {
        session.response(Code.ERR_WASNOSUCHNICK).add(id).send();
      }
      session.response(Code.RPL_ENDOFWHOWAS);
    }
  }
}
