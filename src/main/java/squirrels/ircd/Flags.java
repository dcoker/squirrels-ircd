package squirrels.ircd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters
public class Flags {

  private static final int DEFAULT_IRC_PORT = 6667;

  private static final String DEFAULT_SERVER_NAME = "localhost";
  private static final String DEFAULT_NETWORK_NAME = "squirrels";
  @Parameter(names = {"-port"})
  public Integer port = DEFAULT_IRC_PORT;

  @Parameter(names = {"-server_name"})
  public String serverName = DEFAULT_SERVER_NAME;

  @Parameter(names = {"-network_name"})
  public String networkName = DEFAULT_NETWORK_NAME;
}
