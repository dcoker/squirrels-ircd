package squirrels.ircd;

public final class Strings2 {
  private Strings2() {
  }

  public static String truncate(String data, int length) {
    if (data.length() > length) {
      return new StringBuilder()
          .append(data.substring(0, length - 6))
          .append("...")
          .append(data.substring(data.length() - 3, data.length()))
          .append("[sz=").append(data.length()).append("]").toString();
    }
    return data;
  }
}
