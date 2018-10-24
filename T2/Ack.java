import java.io.*;

class Ack {
	private String clockAck;
	private String rscID;
	private int ackType;
  private String senderPid = null;

	public Ack(BufferedReader inFromClient, int ackType) throws Exception{
		clockAck = inFromClient.readLine();
    if(ackType==2)
      senderPid = inFromClient.readLine();
		rscID = inFromClient.readLine();
		this.ackType = ackType;
	}

	public int getRscID(){
		return Integer.parseInt(rscID);
	}

	public int getAckClock(){
		return Integer.parseInt(clockAck);
	}

	public int getAckType(){
		return ackType;
	}

  public int getSenderPid(){
    return Integer.parseInt(senderPid);
  }
}
