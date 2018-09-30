import java.io.*;

class Message {
	private boolean isAck;
	private String data;
	private String clock;

	public Message(BufferedReader inFromClient){
		String s = inFromClient.readLine();

		if(s.equals("1"))
			isAck = true;
		else
			isAck = false;
			
		clock = inFromClient.readLine();
		data = inFromClient.readLine();
	}
	
	public boolean getAck(){
		return isAck;
	}
	
	public int getGlobalClock(){
		return parseInt(clock);
	}
	
	public int getClock(){
		return Integer.parseInt(clock.substring(0,clock.length));
	}
	
	public String getData(){
		return data;
	}
}
