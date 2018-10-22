import java.io.*;

class Message {
	private String resource;
	private String clock;

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

	public int getResource(){
		return Integer.parseInt(resource);
	}

  public char getSenderPid(){
    return Integer.parseInt(clock.substring(clock.length()-1, clock.length()));
  }
}
