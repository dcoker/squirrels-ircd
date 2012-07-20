package squirrels.ircd;

import com.google.common.base.Strings;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A single-line log formatter. <p/> Output format is:
 * <pre>[severity][thread][timestamp][class][method][message][thrown]</pre>
 * <p/> Enable with -Djava.util.logging.config.file=logging.properties
 */
public class LogFormatter extends Formatter {
  private final DateFormat dateFormatter = new SimpleDateFormat("yyMMddHHmmss");

  @Override
  public String format(LogRecord record) {
    return String.format(
        "%2d %3d %s %-30s %s%s\n",
        record.getLevel().intValue() / 100,
        record.getThreadID(),
        dateFormatter.format(new Date(record.getMillis())),
        Strings.nullToEmpty(record.getSourceClassName()) + "." +
            Strings.nullToEmpty(record.getSourceMethodName()) + "()",
        Strings.nullToEmpty(record.getMessage()),
        formatThrown(record.getThrown()));
  }

  private String formatThrown(Throwable thrown) {
    if (thrown == null) {
      return "";
    } else {
      try {
        StringWriter sw = new StringWriter();
        sw.append("; cause: ");
        try (PrintWriter pw = new PrintWriter(sw)) {
          thrown.printStackTrace(pw);
        }
        return sw.toString();
      } catch (Exception ex) {
        return "bug during formatting: " + ex.getMessage();
      }
    }
  }
}
