Build requirements:
 * maven2
 * jdk6

How to build?

$ mvn package


How to run?

$ java -jar target/mymail-0.0.1-SNAPSHOT-jar-with-dependencies.jar 
usage: org.tuxdna.mail.FetchMail
 -b         fetch message body
 -h <arg>   host
 -i         check only folder: inbox
 -u <arg>   username

Yahoo Account: myusername@yahoo.com
-h imap.mail.yahoo.com -u myusername

Gmail Account: myusername@gmail.com
-h imap.googlemail.com -u myusername@gmail.com


Sample run:

$ java -jar target/mymail-0.0.1-SNAPSHOT-jar-with-dependencies.jar -h imap.mail.yahoo.com -u yahoo_user_name -i
[Password:] ..enter..password..

