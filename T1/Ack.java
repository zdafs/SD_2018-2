import java.io.*;

class Ack {
	private String clockMsg;
	private String clockAck;

	public Ack(BufferedReader inFromClient) throws Exception{
		clockAck = inFromClient.readLine();
		clockMsg = inFromClient.readLine();
	}

	public int getMsgClock(){
		return Integer.parseInt(clockMsg);
	}
	
	public int getAckClock(){
		return Integer.parseInt(clockAck);
	}
}
