import java.io.*;

class Message {
	public boolean isAck;
	public String data;
	public String clock;

	public Message(BufferedReader inFromClient){
		String s = inFromClient.readLine();

		if(s.equals("1"))
			isAck = true;
		else
			isAck = false;

		clock = inFromClient.readLine();
		data = inFromClient.readLine();
	}
}