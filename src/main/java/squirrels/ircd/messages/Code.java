package squirrels.ircd.messages;

public enum Code {
  RPL_WELCOME(1),
  RPL_YOURHOST(2),
  RPL_CREATED(3),
  RPL_MYINFO(4),
  RPL_ISUPPORT(5, "are supported by this server"),
  RPL_UMODEIS(221),
  RPL_STATSDLINE(250),
  RPL_LUSERCLIENT(251),
  RPL_LUSEROP(252, "IRC Operators Online"),
  RPL_LUSERUNKNOWN(253, "Unkown connection(s)"),
  RPL_LUSERCHANNELS(254, "channels formed"),
  RPL_LUSERME(255),
  X_USERCOUNT(265),
  X_GLOBALCOUNT(266),
  RPL_AWAY(301),
  RPL_UNAWAY(305, "You are no longer marked as being away"),
  RPL_NOWAWAY(306, "You have been marked as being away"),
  RPL_WHOISUSER(311),
  RPL_WHOISSERVER(312, "otis is awesome"),
  RPL_WHOWASUSER(314),
  RPL_ENDOFWHO(315, "End of /WHO list"),
  RPL_WHOISIDLE(317, "seconds idle, signon time"),
  RPL_ENDOFWHOIS(318, "End of /WHOIS list"),
  RPL_WHOISCHANNELS(319),
  RPL_LISTSTART(321, "Begin of /LIST"),
  RPL_LIST(322),
  RPL_LISTEND(323, "End of /LIST"),
  RPL_CHANNELMODEIS(324),
  X_CHANNEL_CREATED_TIME(329),
  RPL_NOTOPIC(331, "No topic is set"),
  RPL_TOPIC(332),
  X_TOPIC_LAST_CHANGED_BY(333),
  X_ACTUALLY_USING_HOST(338, "actually using host"),
  RPL_WHOREPLY(352),
  RPL_NAMREPLY(353),
  RPL_ENDOFNAMES(366, "End of /NAMES list"),
  RPL_ENDOFWHOWAS(369, "End of WHOWAS"),
  RPL_MOTDSTART(375, "- Begin /MOTD"),
  RPL_MOTD(372),
  RPL_ENDOFMOTD(376, "End of /MOTD command"),
  ERR_NOSUCHNICK(401, "No such nick/channel"),
  ERR_NOSUCHCHANNEL(403, "No such channel"),
  ERR_CANNOTSENDTOCHAN(404, "Cannot send to channel"),
  ERR_WASNOSUCHNICK(406, "There was no such nickname"),
  ERR_UNKNOWNCOMMAND(421, "Unknown command"),
  ERR_NOMOTD(422, "MOTD file is missing"),
  ERR_NONICKNAMEGIVEN(431, "No nickname given"),
  ERR_ERRONEUSNICKNAME(432, "Erroneus nickname"),
  ERR_NICKNAMEINUSE(433, "Nickname already in use"),
  ERR_NOTONCHANNEL(442, "You're not on that channel"),
  ERR_NEEDMOREPARAMS(461, "Not enough parameters"),
  ERR_BADCHANNELKEY(475, "Cannot join channel (+k)"),
  ERR_USERSDONTMATCH(502, "Cant change mode for other users");

  private final int numeric;
  private String defaultPayload;

  private Code(int numeric, String defaultPayload) {
    this.numeric = numeric;
    this.defaultPayload = defaultPayload;
  }

  private Code(int numeric) {
    this.numeric = numeric;
  }

  public int getNumeric() {
    return numeric;
  }

  public String getDefaultPayload() {
    return defaultPayload;
  }
}
