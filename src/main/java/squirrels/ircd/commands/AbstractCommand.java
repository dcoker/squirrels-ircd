package squirrels.ircd.commands;

public abstract class AbstractCommand implements Command {

  public boolean isHuman() {
    return true;
  }
}
