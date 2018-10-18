import java.io.*;

class Message {
	private String resource;
	private String clock;
	private int quantAcks;

	public Message(BufferedReader inFromClient) throws Exception{
		clock = inFromClient.readLine();
		resource = inFromClient.readLine();
	}
	
	public int getGlobalClock(){
		return Integer.parseInt(clock);
	}
	
	public int getClock(){
		return Integer.parseInt(clock.substring(0,clock.length()-1));
	}
	
	public String getResource(){
		return resource;
	}

	public void receivedAck(){
		quantAcks--;
	}

	public void setQuantAcks(int quant){
		quantAcks = quantAcks - quant;
	}

	public int getQuantAcks(){
		return quantAcks;
	}
}
