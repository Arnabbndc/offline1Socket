package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private static HashMap<String, Worker> workers= new HashMap<>();
    public static HashMap<String,Worker> getWorkers(){
        return workers;
    }
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket welcomeSocket = new ServerSocket(6667);
      //  ServerSocket fileWelcomeSocket = new ServerSocket(7777);

        while(true) {
            System.out.println("Waiting for connection...");
            Socket socket = welcomeSocket.accept();
           // Socket fileSocket = fileWelcomeSocket.accept();

            System.out.println("Connection established");

            // open thread
            Thread worker = new Worker(socket);
     //       Thread fileWorker= new FileWorker(socket, fileSocket);
            worker.start();


        }

    }
}
