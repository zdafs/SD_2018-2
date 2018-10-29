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
  static private int quantRsc;
	static private ResourceManager rscMan[];

	static private final int Rqst = 0;
	static private final int ansAck = 1;
	static private final int ansAckGo = 2;
	static private final int ansNack = 3;

	static private final int working =4;	//utilizando o recurso
	static private final int waiting = 5;	//querendo utilizar o recurso
	static private final int standing = 6;	//não precisa do recuros

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

		for(int i = 0; i<quantRsc; i++) rscMan[i] = new ResourceManager(quant);

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
                    if(data.equals("M")){
                        for(int i=0; i<quantRsc; i++){
                        	if(rscMan[i].getState() == standing)
                            	System.out.print("R"+i+" - standing\n");
                            else if(rscMan[i].getState() == waiting)
                            	System.out.print("R"+i+" - Waiting: "+rscMan[i].Nack()+" in line\n");
                            else
                            	System.out.print("R"+i+" - working\n");
                        }
                        System.out.println("");
                    }
                    else if(data.equals("L")){
                        data = reader.readLine();
                        if(rscMan[Integer.parseInt(data)].getState() == working){
                            try{
                                rscMan[Integer.parseInt(data)].sndMsgResource("192.168.0.10", Integer.parseInt(data), Integer.toString(pid), "0");
                            }
                            catch(Exception e){
                                e.printStackTrace();
                            }
	                        rscMan[Integer.parseInt(data)].setState(standing);
	                        StringBuilder sndMessage = new StringBuilder();
	                        sndMessage.append(Integer.toString(ansAck)+'\n'+Integer.toString(clock)+'\n'+data);
	                        Socket clientSocket;
	                        while(rscMan[Integer.parseInt(data)].size()>0){
	                            int sndPid = rscMan[Integer.parseInt(data)].pop();
	                            clientSocket = new Socket("192.168.0.10", basePort+sndPid);
	                            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
	                            outToServer.writeBytes(sndMessage.toString());
	                            clientSocket.close();
	                        }
	                        System.out.print("R"+data+" - Liberado!\n");
                        }
                        else
                        	System.out.print("R"+data+" não está em uso por este processo!\n");
                    }

                    else if(Integer.parseInt(data) >= 0 && Integer.parseInt(data) < quantRsc){
                        message.append(Integer.toString(Rqst)+'\n'+Integer.toString(clock)+Integer.toString(pid)+'\n'+data);

                        rscMan[Integer.parseInt(data)].setClock(Integer.parseInt(Integer.toString(clock)+Integer.toString(pid)));

                        rscMan[Integer.parseInt(data)].setState(waiting);

                        Socket clientSocket;
                        for(int i=0; i<quant; i++){
                            clientSocket = new Socket("192.168.0.10", basePort+i);
                            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                            outToServer.writeBytes(message.toString());
                            clientSocket.close();
                        }
                    }
                    else
                    	System.out.print("Comando inválido!\n");
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

            String type = inFromClient.readLine();

			if(type.equals(Integer.toString(ansAck))){
				Ack ack = new Ack(inFromClient, ansAck);
				newAck(ack);
			}
			else if((type.equals(Integer.toString(ansNack)))){
		        Ack ack = new Ack(inFromClient, ansNack);
		        newAck(ack);
			}
              else if((type.equals(Integer.toString(ansAckGo)))){
                    Ack ack = new Ack(inFromClient, ansAckGo);
                    newAck(ack);
              }
			else{
				Message rcvMsg = new Message(inFromClient);
				StringBuilder sndMessage = newMsg(rcvMsg);

				Socket clientSocket;
				clientSocket = new Socket("192.168.0.10", basePort+rcvMsg.getSenderPid());
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
        boolean connect = false;

        if(ack.getAckType()==ansAck){
          if(rscMan[ack.getRscID()].RcvAns(true) == 0){
            rscMan[ack.getRscID()].setState(working);
            System.out.print("R"+ack.getRscID()+" - Working\n");
            connect = true;
          }
        }
        else if(ack.getAckType()==ansAckGo){
          if(rscMan[ack.getRscID()].RcvAns(true) == 0){
            rscMan[ack.getRscID()].setState(working);
            System.out.print("R"+ack.getRscID()+" - Working\n");
            connect = true;
          }
          rscMan[ack.getRscID()].add(ack.getSenderPid());
        }
        else
          rscMan[ack.getRscID()].RcvAns(false);

        if(connect){
            try{
                rscMan[ack.getRscID()].sndMsgResource("192.168.0.10", ack.getRscID(), Integer.toString(pid), "1");
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static synchronized StringBuilder newMsg(Message rcvMsg){
        clock = Math.max(rcvMsg.getClock(), clock) + 1;
        StringBuilder sndMessage = new StringBuilder();
        if(rcvMsg.getSenderPid() ==  pid){
            sndMessage.append(Integer.toString(ansAck)+'\n'+Integer.toString(clock)+'\n'+Integer.toString(rcvMsg.getResource()));
        }
        else if(rscMan[rcvMsg.getResource()].getState() == standing){
          sndMessage.append(Integer.toString(ansAck)+'\n'+Integer.toString(clock)+'\n'+Integer.toString(rcvMsg.getResource()));
        }
        else if(rscMan[rcvMsg.getResource()].getState() == working){
          sndMessage.append(Integer.toString(ansNack)+'\n'+Integer.toString(clock)+'\n'+Integer.toString(rcvMsg.getResource()));
          rscMan[rcvMsg.getResource()].add(rcvMsg.getSenderPid());
        }
        else{
          if(rscMan[rcvMsg.getResource()].getClock()<rcvMsg.getGlobalClock()){
            sndMessage.append(Integer.toString(ansNack)+'\n'+Integer.toString(clock)+'\n'+Integer.toString(rcvMsg.getResource()));
            rscMan[rcvMsg.getResource()].add(rcvMsg.getSenderPid());
          }
          else
            sndMessage.append(Integer.toString(ansAckGo)+'\n'+Integer.toString(clock)+'\n'+Integer.toString(pid)+'\n'+Integer.toString(rcvMsg.getResource()));
        }
        return sndMessage;
    }
}
