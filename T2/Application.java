import java.io.*;
import java.net.*;

class Application implements Runnable{
    public Socket connectionSocket;


    public Application(Socket connectionSocket){
        this.connectionSocket = connectionSocket;
    }

    public static void main(String argv[]) throws IOException{
        ServerSocket welcomeSocket = new ServerSocket(5000);

        while(true){
            Socket connectionSocket = welcomeSocket.accept();

            Application aux = new Application(connectionSocket);

            Thread clientThread = new Thread(aux);

            clientThread.start();
        }
    }

    @Override
    public void run(){
        try{
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(this.connectionSocket.getInputStream()));
            StringBuilder s = new StringBuilder();
            s.append("Do processo "+inFromClient.readLine()+": "+inFromClient.readLine());
            System.out.println(s.toString());
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
