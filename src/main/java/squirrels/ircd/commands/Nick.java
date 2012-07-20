package squirrels.ircd.commands;

import com.google.common.annotations.VisibleForTesting;
import squirrels.ircd.Session;
import squirrels.ircd.SessionState;
import squirrels.ircd.Users;
import squirrels.ircd.messages.Code;
import squirrels.ircd.messages.Message;
import squirrels.ircd.messages.ParsedMessage;

public class Nick extends AbstractCommand {
  public static final int MAX_NICK_LENGTH = 30;

  private final Users users;

  Nick(Users users) {
    this.users = users;
  }

  @Override
  public void execute(ParsedMessage command, Session session) {
    String oldNick = session.getNick();
    if (command.getPayload() == null && command.getParameters() == null) {
      session.response(Code.ERR_NONICKNAMEGIVEN).send();
      return;
    }
    String newNick = command.getParameters() != null
        ? command.getParameters()[0]
        : command.getPayload();
    if (!isValidNickname(newNick)) {
      session.response(Code.ERR_ERRONEUSNICKNAME).add(newNick).send();
      return;
    }

    if (users.getSession(newNick) != null) {
      session.response(Code.ERR_NICKNAMEINUSE).send();
      return;
    }

    session.setNick(newNick);
    if (session.getState() == SessionState.PREREGISTRATION) {
      // nothing to do. Protocol will send welcome message.
    } else {
      session.write(new Message().setPrefix(oldNick + "!~" + session.getUsername() +
          "@" + session.getActualRemoteHost()).add("NICK").setPayload(newNick));
    }
  }

  @VisibleForTesting
  static boolean isValidNickname(String nickname) {
    return nickname.toLowerCase().matches("^[a-z_\\[\\]{}\\|`^]+[a-z0-9_\\[\\]{}\\|`^-]*$") &&
        nickname.length() <= MAX_NICK_LENGTH;
  }
}
