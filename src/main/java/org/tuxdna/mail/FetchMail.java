package org.tuxdna.mail;

import java.io.Console;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class FetchMail {
	private static long start = System.currentTimeMillis();
	private static String FOLDER_INVALID = "folder cannot contain messages";
	private static String CONNECTION_FAILURE = "connection failure";

	private static void help(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(FetchMail.class.getName(), options);
	}

	private static void info(String s) {
		System.out.println(s);
	}

	private static void timeIt(String s) {
		long end = System.currentTimeMillis();
		System.out.println(s + " - time:" + (end - start) + "ms");
		start = end;
	}

	public static void main(String[] args) {

		// create Options object
		Options options = new Options();
		options.addOption("u", true, "username");
		options.addOption("h", true, "host");
		options.addOption("b", false, "fetch message body");
		options.addOption("i", false, "check only folder: inbox");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(-1);
		}

		String username = null;
		String host = "imap.gmail.com";
		boolean fetchBody = false;
		boolean fetchOnlyInbox = false;
		
		if (cmd.hasOption("u")) {
			username = cmd.getOptionValue("u");
		} else {
			help(options);
			System.exit(-1);
		}

		if (cmd.hasOption("h")) {
			host = cmd.getOptionValue("h");
		}

		if (cmd.hasOption("b")) {
			fetchBody = true;
		}

		if (cmd.hasOption("i")) {
			fetchOnlyInbox= true;
		}

		Console cons = null;
		char[] passwd = null;
		if ((cons = System.console()) != null
				&& (passwd = cons.readPassword("[%s]", "Password:")) != null) {
		} else {
			System.err.println("Reading password failed!");
			System.exit(-1);
		}

		String password = new String(passwd);
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");

		try {
			Session session = Session.getDefaultInstance(props, null);
			Store store = session.getStore("imaps");
			info("Connecting to server: " + host);
			store.connect(host, username, password);
			info("Connected.");

			Folder default_folder = store.getDefaultFolder();

			info("Searching for all folders");
			for (Folder folder : default_folder.list()) {
				String folder_name = folder.getName();
				info("Opening folder: " + folder_name);
				
				if(fetchOnlyInbox &&
					! folder_name.equalsIgnoreCase("inbox") ) {
					info("this is not inbox, skipping it...");
					continue;
				}
				
				try {
					start = System.currentTimeMillis();
					folder.open(Folder.READ_WRITE);
					info("Opened.");
					timeIt("Opening folder ");
					int count = folder.getMessageCount();
					System.out.println("Total messages: " + count);
					timeIt("Getting folder count ");
					info("Getting all messages now... be patient");
					if (fetchBody) {
						info("I will also fetch the body part of each message...");
					}

					int batch_size = 10;
					for (int msg_id = 1; msg_id <= count; msg_id += batch_size) {
						int l = msg_id;
						int u = (msg_id + batch_size - 1) < count ? msg_id
								+ batch_size - 1 : count;
						Message messages[] = folder.getMessages(l, u);
						for (Message m : messages) {
							Enumeration<Header> headers = m.getAllHeaders();
							while (headers.hasMoreElements()) {
								Header h = headers.nextElement();
							}
							if (fetchBody) {
								try {
									Object m_obj = m.getContent();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						timeIt("Getting messages from " + l + " to " + u);
					}
				} catch (MessagingException e) {

					String msg = e.getMessage();
					if (msg.equals(FOLDER_INVALID)) {
						info("Perhaps an invalid folder");
					} else if (msg.equals(CONNECTION_FAILURE)) {
						info("Connection failed. consider RECONNECT.");
					} else {
						info("Unknow failure");
						e.printStackTrace();
					}
				}

				info(""); // just a blank line
			}
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (MessagingException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}
}
