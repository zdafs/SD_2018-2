import java.io.*;

class Ack {
	private String clockMsg;
	private String clockAck;

	public Ack(BufferedReader inFromClient){
		clockAck = inFromClient.readLine();
		clockMsg = inFromClient.readLine();
	}

	public int getMsgClock(){
		return parseInt(clockMsg);
	}
	
	public int getAckClock(){
		return parseInt(clockAck);
	}
}
