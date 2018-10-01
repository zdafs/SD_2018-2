import java.io.*;
import java.net.*;
import java.util.*;

class Process implements Runnable{
	static private int clock;
	static private int pid;
	static private int quant;
	static private int basePort;
	static private Socket clientSocket;
	private Socket connectionSocket;
	static private LinkedList<Message> msgList;
	static private Hashtable<Integer, Integer> freeAcks;

	public Process(Socket connectionSocket){
		this.connectionSocket = connectionSocket;
	}

	public static void main(String argv[]) throws IOException{
		clock = Integer.parseInt(argv[0]);
		pid = Integer.parseInt(argv[1]);
		quant = Integer.parseInt(argv[2]);
		basePort = 7000;
		msgList = new LinkedList<Message>();
		freeAcks = new Hashtable<Integer, Integer>();
		new Thread(enviaMensagem).start();
		new Thread(recebeMensagem).start();
	}

	private static Runnable enviaMensagem = new Runnable() {
		public void run(){
			try{
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				while(true){
					StringBuilder message = new StringBuilder();
					String data = reader.readLine();
					message.append("0"+'\n'+Integer.toString(clock)+Integer.toString(pid)+'\n'+data+'\n');
					for(int i=0; i<quant; i++){
						clientSocket = new Socket("192.168.0.12", basePort+i);
						DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
						outToServer.writeBytes(message.toString());
						clientSocket.close();
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	};

	private static Runnable recebeMensagem = new Runnable() {
		public void run(){
			try{
				ServerSocket welcomeSocket = new ServerSocket(basePort+pid);

				while(true){
					Socket connectionSocket = welcomeSocket.accept();

					Process aux = new Process(connectionSocket);

					new Thread(aux).start();
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	};

	public void run(){
		try{
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

			if(inFromClient.readLine().equals("1")){
				Ack ack = new Ack(inFromClient);
				clock = Math.max(ack.getAckClock(), clock) + 1;
				int i=0;
				while(i<msgList.size() && msgList.get(i).getGlobalClock()<ack.getMsgClock())
					i++;
				if(i<msgList.size() && msgList.get(i).getGlobalClock()==ack.getMsgClock()){
					msgList.get(i).receivedAck();
					checkQueue();
				}
				else{
					if(freeAcks.isEmpty() || !freeAcks.containsKey(ack.getMsgClock())){
						freeAcks.put(ack.getMsgClock(), 1);
					}
					else{
						freeAcks.put(ack.getMsgClock(), freeAcks.get(ack.getMsgClock())+1);
					}
				}
			}
			else{
				Message rcvMsg = new Message(inFromClient, quant);
				clock = Math.max(rcvMsg.getClock(), clock) + 1;
				int i=0;
				while(i<msgList.size() && msgList.get(i).getGlobalClock()<rcvMsg.getGlobalClock())
					i++;
				msgList.add(i, rcvMsg);
				if(!freeAcks.isEmpty() && freeAcks.containsKey(rcvMsg.getGlobalClock())){
					rcvMsg.setQuantAcks(freeAcks.get(rcvMsg.getGlobalClock()));
					freeAcks.remove(rcvMsg.getGlobalClock());
				}
				StringBuilder sndMessage = new StringBuilder();
				sndMessage.append("1"+'\n'+Integer.toString(clock)+'\n'+Integer.toString(rcvMsg.getGlobalClock()));
				for(i=0; i<quant; i++){
					clientSocket = new Socket("192.168.0.12", basePort+i);
					DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
					outToServer.writeBytes(sndMessage.toString());
					clientSocket.close();
				}
			}

		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public void checkQueue(){
		Message m;
		while(msgList.size()>0 && msgList.peekFirst().getQuantAcks()==0){
			m = msgList.remove();
			System.out.println("A mensagem \""+m.getData()+"\", com tempo "+m.getGlobalClock()+", recebeu "+quant+" ACKs");
		}
	}
}