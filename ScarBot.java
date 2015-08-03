/*
 *	 ScarBot v 1.0
 *
 * 	 Copyright 2015 Sergio Carrozzo
 * 
 *   This file is part of ScarBot.
 * 
 *   ScarBot is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   ScarBot is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with ScarBot.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.net.*;
import java.io.*;
import java.util.regex.*;
import java.util.GregorianCalendar;

class ScarBot implements Runnable
{
	/* --------------------- EDITABLE PART ---------------------*/
	/* Configuration */
    	private String botnick = "ScarBot";
	private String login = "ScarBot";
	private String channel = "#channel_name";
	private String admin = "admin_nickname";
	/* End of configuration  */
	/* --------------------- END EDITABLE PART ---------------------*/
	
	
	private Socket s;
	private BufferedReader r;
	private BufferedWriter w;
	private String line = null;
	
	/*	Constructor to be used to launch only one bot	*/
	public ScarBot(String host, int port) throws Exception
	{
		// open the connection
		try
		{
			s = new Socket(host, port);
			r = new BufferedReader(new InputStreamReader(s.getInputStream()));
			w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
			System.err.println("Host not found");
			System.exit(1);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.err.println("Error connecting to the host");
			System.exit(1);
		}
	}
	
	/* For multiple bots */
	public ScarBot(String host, int port, String botnick, String login) throws Exception
	{
		// allow to overwrite botnick and login name for each bot/thread
		this.botnick = botnick;
		this.login = login;
		
		// open the connection
		try
		{
			s = new Socket(host, port);
			r = new BufferedReader(new InputStreamReader(s.getInputStream()));
			w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
			System.err.println("Host not found");
			System.exit(1);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.err.println("Error connecting to the host");
			System.exit(1);
		}
	}

	// main thread execution
	public void run()
	{
		try
		{		
			// send nickname and user to the server
			w.write("NICK "+botnick+"\r\n");
			w.write("USER "+login+" "+getIP()+": Java Bot\r\n");
			w.flush();
			
			// wait until we are logged in
			while ((line = r.readLine( )) != null) {
				System.out.println(line);
			  	  if (line.indexOf("MODE") >= 0) {
			  	  break;
				}
			}
			
			// join to channel
			w.write("JOIN "+channel+"\r\n");
			w.flush();
			
			// loop until the bot receive responses from server
			while((line = r.readLine()) != null)
			{
				System.out.println(line);
				Pattern admin_regex = Pattern.compile("^:"+admin+"\\b", Pattern.CASE_INSENSITIVE);
				Matcher admin = admin_regex.matcher(line);
				
				// send ping response
				ping_pong(line);
				
				// print the bot functionalities
				help();
				
				// disconnect from server
				exit(admin);
				
				// change the channel
				join(admin);
				
				// print date information
				date();
				
				// set some modes to a user
				op(admin);
				
				// remove some modes to a user
				deop(admin);
				
				// kick or ban a user
				kick_ban(admin);
			}
			s.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.err.println("Error in run() execution");
			System.exit(1);
		}
	}
	
	
	private String getIP() throws Exception
	{
		InetAddress localhost = InetAddress.getLocalHost();
		String ip = localhost.getHostAddress();
		return(ip);
	}
	
	
	/*	Functions of ScarBot	*/
	
	private void ping_pong(String line) throws IOException
	{
		Pattern ping_regex = Pattern.compile("^PING",Pattern.CASE_INSENSITIVE);
		Matcher ping = ping_regex.matcher(line);	
		if(ping.find())
		{
			System.out.println("Sto inviando PONG "+line.substring(5));
			w.write("PONG "+ line.substring(5) +" \r\n");
			//w.write("PONG "+channel+"\r\n");
			w.flush();
		}
	}
	
	private void exit(Matcher admin) throws IOException
	{
		Pattern exit_regex = Pattern.compile("!exit", Pattern.CASE_INSENSITIVE);
		Matcher exit = exit_regex.matcher(line);
		if(exit.find() && admin.find())
		{
			w.write("PRIVMSG "+ channel + " :Bye :P\r\n");
			w.write("QUIT "+channel+ "\r\n");
			w.flush();
		}
	}
	
	private void join(Matcher admin) throws IOException
	{
		Pattern join_regex = Pattern.compile("!join", Pattern.CASE_INSENSITIVE);
		Matcher join = join_regex.matcher(line);
		try
		{
		if(join.find() && admin.find())
		{
			String svect[] = line.split(" ");
			if(svect[4]!=null)
			  w.write("PART "+channel+" \r\n");
			channel = svect[4];
			w.write("JOIN "+channel+" \r\n");
			w.write("PRIVMSG "+channel+" :Hello! @.@\r\n");
			w.flush();
		}
		}
		catch(Exception e)
		{
			w.write("PRIVMSG "+channel+" :Error.Usage !join #channel\r\n");
			w.flush();
		}
	}
	
	private void date() throws IOException
	{
		Pattern date_regex = Pattern.compile("!date",Pattern.CASE_INSENSITIVE);
		Matcher date = date_regex.matcher(line);
		if(date.find())
		{
			GregorianCalendar cl = new GregorianCalendar();
			String str_date = cl.get(cl.DAY_OF_MONTH)+"/"+(cl.get(cl.MONTH)+1)+"/"+cl.get(cl.YEAR)+" --- "+cl.get(cl.HOUR_OF_DAY)+"."+cl.get(cl.MINUTE)+"."+cl.get(cl.SECOND);
			w.write("PRIVMSG "+channel+" :"+str_date+"\r\n");
			w.flush();
		}
	}
	
	private void op(Matcher admin) throws IOException
	{
		Pattern grade_regex[] = {null, null, null, null, null};
		Matcher grade[] = {null, null, null, null, null};
		grade_regex[0] = Pattern.compile("!voice",Pattern.CASE_INSENSITIVE);
		grade[0] = grade_regex[0].matcher(line);
		grade_regex[1] = Pattern.compile("!halfop",Pattern.CASE_INSENSITIVE);
		grade[1] = grade_regex[1].matcher(line);
		grade_regex[2] = Pattern.compile("!op",Pattern.CASE_INSENSITIVE);
		grade[2] = grade_regex[2].matcher(line);
		grade_regex[3] = Pattern.compile("!protect",Pattern.CASE_INSENSITIVE);
		grade[3] = grade_regex[3].matcher(line);
		grade_regex[4] = Pattern.compile("!owner",Pattern.CASE_INSENSITIVE);
		grade[4] = grade_regex[4].matcher(line);
		
		try
		{
		if((grade[0].find() || grade[1].find() || grade[2].find() || grade[3].find() || grade[4].find()) && admin.find())
		{
			String svect[] = line.split(" ");
			if(svect[3].equalsIgnoreCase(":!voice"))
			{
				w.write("MODE "+channel+" +v "+svect[4]+"\r\n");
				w.flush();
			}
			if(svect[3].equalsIgnoreCase(":!halfop"))
			{
				w.write("MODE "+channel+" +h "+svect[4]+"\r\n");
				w.flush();
			}
			if(svect[3].equalsIgnoreCase(":!op"))
			{
				w.write("MODE "+channel+" +o "+svect[4]+"\r\n");
				w.flush();
			}
			if(svect[3].equalsIgnoreCase(":!protect"))
			{
				w.write("MODE "+channel+" +a "+svect[4]+"\r\n");
				w.flush();
			}
			if(svect[3].equalsIgnoreCase(":!owner"))
			{
				w.write("MODE "+channel+" +q "+svect[4]+"\r\n");
				w.flush();
			}
		}
		}
		catch(Exception e)
		{
			w.write("PRIVMSG "+channel+" :Error.Usage !grade Nickname\r\n");
			w.flush();
		}
	}
	

	private void deop(Matcher admin) throws IOException
	{
		Pattern grade_regex[] = {null, null, null, null, null};
		Matcher grade[] = {null, null, null, null, null};
		grade_regex[0] = Pattern.compile("!devoice",Pattern.CASE_INSENSITIVE);
		grade[0] = grade_regex[0].matcher(line);
		grade_regex[1] = Pattern.compile("!dehalfop",Pattern.CASE_INSENSITIVE);
		grade[1] = grade_regex[1].matcher(line);
		grade_regex[2] = Pattern.compile("!deop",Pattern.CASE_INSENSITIVE);
		grade[2] = grade_regex[2].matcher(line);
		grade_regex[3] = Pattern.compile("!deprotect",Pattern.CASE_INSENSITIVE);
		grade[3] = grade_regex[3].matcher(line);
		grade_regex[4] = Pattern.compile("!deowner",Pattern.CASE_INSENSITIVE);
		grade[4] = grade_regex[4].matcher(line);
		
		try
		{
		if((grade[0].find() || grade[1].find() || grade[2].find() || grade[3].find() || grade[4].find()) && admin.find())
		{
			String svect[] = line.split(" ");
			if(svect[3].equalsIgnoreCase(":!devoice"))
			{
				w.write("MODE "+channel+" -v "+svect[4]+"\r\n");
				w.flush();
			}
			if(svect[3].equalsIgnoreCase(":!dehalfop"))
			{
				w.write("MODE "+channel+" -h "+svect[4]+"\r\n");
				w.flush();
			}
			if(svect[3].equalsIgnoreCase(":!deop"))
			{
				w.write("MODE "+channel+" -o "+svect[4]+"\r\n");
				w.flush();
			}
			if(svect[3].equalsIgnoreCase(":!deprotect"))
			{
				w.write("MODE "+channel+" -a "+svect[4]+"\r\n");
				w.flush();
			}
			if(svect[3].equalsIgnoreCase(":!deowner"))
			{
				w.write("MODE "+channel+" -q "+svect[4]+"\r\n");
				w.flush();
			}
		}
		}
		catch(Exception e)
		{
			w.write("PRIVMSG "+channel+" :Error.Usage !degrade Nickname\r\n");
			w.flush();
		}
	}
	
	private void kick_ban(Matcher admin) throws IOException
	{
		Pattern kick_regex = Pattern.compile("!kick",Pattern.CASE_INSENSITIVE);
		Matcher kick = kick_regex.matcher(line);
		Pattern ban_regex = Pattern.compile("!ban",Pattern.CASE_INSENSITIVE);
		Matcher ban = ban_regex.matcher(line);
		Pattern unban_regex = Pattern.compile("!unban",Pattern.CASE_INSENSITIVE);
		Matcher unban = unban_regex.matcher(line);
		try
		{
		if(kick.find() && admin.find())
		{
			String svect[] = line.split(" ");
			w.write("KICK "+channel+" "+svect[4]+"\r\n");
			w.flush();
		}
		if(ban.find() && admin.find())
		{
			String svect[] = line.split(" ");
			w.write("MODE "+channel+" +b "+svect[4]+"\r\n");
			w.flush();
		}
		if(unban.find() && admin.find())
		{
			String svect[] = line.split(" ");
			w.write("MODE "+channel+" -b "+svect[4]+"\r\n");
			w.flush();
		}
		}
		catch(Exception e)
		{
			w.write("PRIVMSG "+channel+" :Error.Usage (!kick or !ban or !unban)  Nickname\r\n");
			w.flush();
		}
	}
	
	private void help()
	{
		Pattern help_regex = Pattern.compile("!help",Pattern.CASE_INSENSITIVE);
		Matcher help = help_regex.matcher(line);
		try
		{
		if(help.find())
		{
			String strhelp = "9Option for admin:0-1)!join #[chan]-2)!exit-3)![grade] [nick]-4)![degrade] [nick]-5)!kick [nick]-6)!ban [nick]-9Option for all users:0-1)!date-2)!help";
			String svect[] = strhelp.split("-");
			for(int i=0;i<svect.length;i++)
			{
				w.write("PRIVMSG "+channel+" :"+svect[i]+"\r\n");
			}
			w.flush();
		}
		}
		catch(IOException e)
		{
			System.out.println("help() error");
		}
	}
}
