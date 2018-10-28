import java.io.*;
import java.net.*;

class Resource{
    static private int rid;

    public static void main(String argv[]) throws IOException{
        rid = Integer.parseInt(argv[0]);
        ServerSocket welcomeSocket = new ServerSocket(5000+rid);

        while(true){
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            String op = inFromClient.readLine();
            String pid = inFromClient.readLine();
            if(op.equals("1"))
                System.out.println("O processo "+pid+" está utilizando o recurso");
            else
                System.out.println("O processo "+pid+" deixou de utilizar o recurso");
        }
    }
}
