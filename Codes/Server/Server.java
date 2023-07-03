package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private static HashMap<String, Worker> workers= new HashMap<>();
    public static HashMap<String,Worker> getWorkers(){
        return workers;
    }
    public  static HashMap<String, Integer> fileIds= new HashMap<>();
    public  static HashMap<String, String> reqIds= new HashMap<>();
    public static long MAX_BUFFER_SIZE = 100000 * 1024; // byte
    public static int MIN_CHUNK_SIZE = 5; // kilobyte
    public static int MAX_CHUNK_SIZE = 50; // kilobyte
    public static volatile long CUR_BUFFER_SIZE = 0;
    public static void main(String[] args) throws IOException{
        System.out.println("Process can be finished by pressing ctrl+D ....");
          ServerSocket welcomeSocket = new ServerSocket(6667);
          ServerSocket welcomeSocketFile = new ServerSocket(7777);
        Thread thread = new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                while (reader.readLine() != null) {}

                System.out.println("Program is ending.");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread.start();
            while (true) {
                System.out.println("Waiting for connection...");
                Socket socket = welcomeSocket.accept();
                Socket socketFile = welcomeSocketFile.accept();
                System.out.println("Connection established");
                Thread worker = new Worker(socket, socketFile);
                worker.start();
            }




    }
}
