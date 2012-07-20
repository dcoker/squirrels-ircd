package squirrels.ircd;

public interface NickChangeListener {
  public void nickChange(Session session, String oldIdentity, String oldNick);
}
