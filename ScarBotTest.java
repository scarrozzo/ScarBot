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

class ScarBotTest
{
	public static void main(String args[])
	{
		if(args.length<3)
		{
			System.out.println();
			String s = "===================== Error ====================="
				   +"\nUsage: java ScarBotTest <server> <port> <num_bot>\n"
				   +"=================================================\n";
			System.out.println(s);
			System.exit(1);
		}
		try
		{
			int num_bot = Integer.parseInt(args[2]);
			if(num_bot == 1){
				ScarBot b = new ScarBot(args[0], Integer.parseInt(args[1]));
				Thread t = new Thread(b);
				t.start();
			}
			else{
				for(int i = 0; i < num_bot; i++){
					String randomName = "ScarBot_"+((int)(Math.random()*10000))+"";
					ScarBot b = new ScarBot(args[0], Integer.parseInt(args[1]), randomName, randomName);
					Thread t = new Thread(b);
					t.start();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Error in starting the Bot !");
			System.exit(1);
		}
	}
}
