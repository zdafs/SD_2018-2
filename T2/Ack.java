import java.io.*;

class Ack {
	private String clockMsg;
	private String rscID;
	static private boolean isNack;

	public Ack(BufferedReader inFromClient, boolean isNack) throws Exception{
		clockAck = inFromClient.readLine();
		rscID = inFromClient.readLine();
		this.isNack = isNack;
	}

	public int getRscID(){
		return Integer.parseInt(clockMsg);
	}
	
	public int getAckClock(){
		return Integer.parseInt(clockAck);
	}

	public boolean getIsNack(){
		return isNack;
	}
}
