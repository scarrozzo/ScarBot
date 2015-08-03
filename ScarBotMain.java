class ScarBotMain
{
	public static void main(String args[])
	{
		if(args.length<3)
		{
			System.out.println();
			String s = "===================== Error ====================="
					   +"\nUsage: java Client <server> <port> <num_bot>\n"
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