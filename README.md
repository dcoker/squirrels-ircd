About
=====
Squirrels-IRCd is an experimental NIO-based IRC server implemented in Java. This
implementation is not fully compliant with the IRC RFCs and it does not implement all common IRC
behavior. It has been tested to work with numerous IRC clients.

Prerequisites
=============
This build has only been tested on Ubuntu 11.10. You will need a JDK and Maven:

    $ sudo apt-get install openjdk-7-jdk maven2

Building & Running
==================
This will run tests and build a single .jar file containing all dependencies:

    $ mvn package

You can see the contents of this bundle:

    $ jar tf target/ircd-1.0-jar-with-dependencies.jar

Start the server with:

    $ java -Djava.util.logging.config.file=logging.properties \
      -jar target/ircd-1.0-jar-with-dependencies.jar

You can now connect with an IRC client of your choice to 127.0.0.1. If you want
to run on a different port, pass a -port flag. Example:

    $ java -Djava.util.logging.config.file=logging.properties \
      -jar target/ircd-1.0-jar-with-dependencies.jar -port 1234

Working in IntelliJ
===================
Maven can generate an IntelliJ project file for you:

    $ mvn idea:idea

Resources
=========
  * [MIRC Numeric Reference](http://www.mirc.net/raws/)
  * [RFC1459](http://tools.ietf.org/html/rfc1459)
  * [RFC2810](http://tools.ietf.org/html/rfc2810)
  * [RFC2812](http://tools.ietf.org/html/rfc2812)

