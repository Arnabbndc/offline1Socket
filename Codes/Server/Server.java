package Server;

import java.io.BufferedReader;
import java.io.File;
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
    public static void main(String[] args) throws IOException, ClassNotFoundException {

            ServerSocket welcomeSocket = new ServerSocket(6667);
            //  ServerSocket fileWelcomeSocket = new ServerSocket(7777);
//        File file= new File("Codes/Server/files");
//        file.delete();
//        file.mkdir();
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            public void run() {
//                file.deleteOnExit();
//            }
//        });
        Thread thread = new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                while (reader.readLine() != null) {
                    // Continue reading until Ctrl+Z (Ctrl+D on Windows) is pressed
                }
                // Perform cleanup or any necessary actions before program ends
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
                // Socket fileSocket = fileWelcomeSocket.accept();

                System.out.println("Connection established");

                // open thread
                Thread worker = new Worker(socket);
                //       Thread fileWorker= new FileWorker(socket, fileSocket);
                worker.start();


            }




    }
}
