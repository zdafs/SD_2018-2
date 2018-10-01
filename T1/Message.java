import java.io.*;

class Message {
	private String data;
	private String clock;
	private int quantAcks;

	public Message(BufferedReader inFromClient, int quant) throws Exception{
		clock = inFromClient.readLine();
		data = inFromClient.readLine();
		quantAcks = quant;
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
