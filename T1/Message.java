import java.io.*;

class Message {
	private boolean isAck;
	private String data;
	private String clock;

	public Message(BufferedReader inFromClient) throws Exception{
		clock = inFromClient.readLine();
		data = inFromClient.readLine();
	}
	
	public int getGlobalClock(){
		return Integer.parseInt(clock);
	}
	
	public int getClock(){
		return Integer.parseInt(clock.substring(0,clock.length()-1));
	}
	
	public String getData(){
		return data;
	}
}
