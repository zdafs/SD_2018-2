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
	private ResourceManager rscMan[];

	static private final int Rqst = 0;
	static private final int ansAck = 1;
	static private final int ansNack = 2;

	static private final int working =3;	//utilizando o recurso
	static private final int waiting = 4;	//querendo utilizar o recurso
	static private final int standing = 5;	//não precisa do recuros

	static private int basePort;
	private Socket connectionSocket;

    static private  boolean TESTE = true;

	public Process(Socket connectionSocket){
		this.connectionSocket = connectionSocket;
	}

	public static void main(String argv[]) throws IOException{
		clock = Integer.parseInt(argv[0]);
		pid = Integer.parseInt(argv[1]);
		quant = Integer.parseInt(argv[2]);
		quantRsc = Integer.parseInt(argv[3]);
		rscMan = new ResourceManager[quantRsc];
		basePort = 7000;

		for(int i = 0; ++i<quantRsc;rscMan[i](0, quant));

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
					message.append(Integer.toString(Rqst)+'\n'+Integer.toString(clock)+Integer.toString(pid)+'\n'+data+'\n');

					rscMan[Integer.parseInt(data)].setClock(Integer.toString(clock)+Integer.toString(pid));

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

			if(inFromClient.readLine().equals(ansAck)){
				Ack ack = new Ack(inFromClient, false);
				newAck(ack);
			}
			else if((inFromClient.readLine().equals(ansNack))){
		        Ack ack = new Ack(inFromClient, true);
		        newAck(ack);
			}
			else{
				Message rcvMsg = new Message(inFromClient);
				newMsg(rcvMsg);
				StringBuilder sndMessage = new StringBuilder();
				if(rscMan.getState() == standing){
					sndMessage.append(Integer.toString(ansAck)+'\n'+Integer.toString(clock)+'\n'+Integer.toString(rcvMsg.getResource()));
				}
				else if(rscMan.getState() == working){
					sndMessage.append(Integer.toString(ansNack)+'\n'+Integer.toString(clock)+'\n'+Integer.toString(rcvMsg.getResource()));
					rscMan[rcvMsg.getResource()].add(rcvMsg.getSenderPid());
				}
				Socket clientSocket;
				clientSocket = new Socket("200.9.84.161", basePort+i);
				DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
				outToServer.writeBytes(sndMessage.toString());
				clientSocket.close();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

    public static synchronized void newAck(Ack ack){
        clock = Math.max(ack.getAckClock(), clock) + 1;
        int i=0;

        if(ack.getIsNack){
          if(rscMan[ack.getRscID()].RcvAns(true) == 0){
            rscMan[ack.getRscID()].startWork();
            //Começa a utilizar o recurso
          }
        }
        else
          rscMan[ack.getRscID()].RcvAns(false);
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
