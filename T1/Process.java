import java.io.*;
import java.net.*;

class Process {
	static private int clock;
	static private int pid;
	static private int quant;
	static private int basePort;
	static private Socket clientSocket;
	static private Socket connectionSocket;

	public static void main(String argv[]) throws IOException{
		clock = Integer.parseInt(argv[0]);
		pid = Integer.parseInt(argv[1]);
		quant = Integer.parseInt(argv[2]);
		basePort = 7000;
		new Thread(enviaMensagem).start();
		new Thread(recebeMensagem).start();
	}

	private static Runnable enviaMensagem = new Runnable() {
		public void run(){
			try{
				BufferedReader data = new BufferedReader(new InputStreamReader(System.in));
				while(true){
					StringBuilder message = new StringBuilder();
					message.append("0"+'\n'+Integer.toString(clock)+Integer.toString(pid)+'\n'+data.readLine()+'\n');
					for(int i=0; i<quant; i++){
						clientSocket = new Socket("200.18.101.31", basePort+i);
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
					connectionSocket = welcomeSocket.accept();

					new Thread(trataMensagem).start();
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	};

	private static Runnable trataMensagem = new Runnable() {
		public void run(){
			try{
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

				//DataOutputStream outToClient = new DataOutputStream(this.connectionSocket.getOutputStream());

				Message m = new Message(inFromCLient)

				String aux = inFromClient.readLine();
				while(aux!=null){
					System.out.println(aux);
					aux = inFromClient.readLine();
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	};
}