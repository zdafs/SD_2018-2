/*
	Lucca La Fonte Albuquerque Carvalho - 726563
	Rafael Felipe Dias dos Santos - 726582
*/

import java.io.*;
import java.net.*;
import java.util.*;

class Process implements Runnable{
	static private int clock;
	static private int pid;
	static private int quant;

	static private int basePort;
	private Socket connectionSocket;

	static private LinkedList<Message> msgList;
	static private Hashtable<Integer, Integer> freeAcks;

    static private  boolean TESTE=true;

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
                    Socket clientSocket;
					for(int i=0; i<quant; i++){
                        clientSocket = new Socket("200.9.84.161", basePort+i);
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
				newAck(ack);
			}
			else{
				Message rcvMsg = new Message(inFromClient, quant);
				newMsg(rcvMsg);
				StringBuilder sndMessage = new StringBuilder();
				sndMessage.append("1"+'\n'+Integer.toString(clock)+'\n'+Integer.toString(rcvMsg.getGlobalClock()));
                Socket clientSocket;
				for(int i=0; i<quant; i++){
					clientSocket = new Socket("200.9.84.161", basePort+i);
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
//newAck
//newMsg
    public static synchronized void newAck(Ack ack){
        clock = Math.max(ack.getAckClock(), clock) + 1;
        int i=0;
        if(TESTE)
            System.out.print("Chegou ACK para a mensagem com clock "+ ack.getMsgClock());
        while(i<msgList.size() && msgList.get(i).getGlobalClock()<ack.getMsgClock()) i++;
        if(i<msgList.size() && msgList.get(i).getGlobalClock()==ack.getMsgClock()){
            if(TESTE)
                System.out.println("\""+msgList.get(i).getData()+"\"");
            msgList.get(i).receivedAck();
            checkQueue();
        }
        else{
            if(TESTE)
                System.out.println("");
            if(freeAcks.isEmpty() || !freeAcks.containsKey(ack.getMsgClock())){
                freeAcks.put(ack.getMsgClock(), 1);
            }
            else{
                freeAcks.put(ack.getMsgClock(), freeAcks.get(ack.getMsgClock())+1);
            }
        }
    }

    public static synchronized void newMsg(Message rcvMsg){
        clock = Math.max(rcvMsg.getClock(), clock) + 1;
        int i=0;
        while(i<msgList.size() && msgList.get(i).getGlobalClock()<rcvMsg.getGlobalClock()) i++;
        msgList.add(i, rcvMsg);
        if(!freeAcks.isEmpty() && freeAcks.containsKey(rcvMsg.getGlobalClock())){
            rcvMsg.setQuantAcks(freeAcks.get(rcvMsg.getGlobalClock()));
            freeAcks.remove(rcvMsg.getGlobalClock());
        }
    }

	public static void checkQueue(){
		Message m;
		while(msgList.size()>0 && msgList.peekFirst().getQuantAcks()==0){
			m = msgList.remove();
			System.out.println("A mensagem \""+m.getData()+"\", com tempo "+m.getGlobalClock()+", recebeu "+quant+" ACKs");
            StringBuilder sndMessage = new StringBuilder();
            sndMessage.append(Integer.toString(pid)+'\n'+m.getData());
            try{
                Socket clientSocket = new Socket("200.9.84.161", 5000);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                outToServer.writeBytes(sndMessage.toString());
                clientSocket.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
		}
	}
}
