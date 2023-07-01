package Client;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;
import Server.Pair;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("localhost", 6667);
        System.out.println("Connection established");
        System.out.println("Remote port: " + socket.getPort());
        System.out.println("Local port: " + socket.getLocalPort());

        // buffers
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());

        String textFromServer =  in.readUTF();
        System.out.println("Text from server: "+textFromServer);
        Scanner scanner= new Scanner(System.in);
        String username= scanner.next();
        out.writeUTF(""+username);
        textFromServer =  in.readUTF();
        System.out.println("Text from server: "+textFromServer);
        if(textFromServer.equals("You are already logged in")){
            System.out.println("login failed. Quitting");
            socket.close();
            return;
        }
        //
        while(true) {
//            String msg = (String) in.readObject();
//            System.out.println(msg);
    //        textFromServer =  in.readUTF();
     //       System.out.println("Text from server: "+textFromServer);
//            scanner= new Scanner(System.in);
//            String username= scanner.next();
//            out.writeUTF(""+username);
//            textFromServer =  in.readUTF();
//            System.out.println("Text from server: "+textFromServer);
//            if(textFromServer.equals("You are already logged in")){
//                System.out.println("login failed. Quitting");
//                socket.close();
//                return;
//            }
            System.out.println("Console ...");
            System.out.println("Choose an option between 1-6");
            System.out.println("1. Lookup all clients");
            System.out.println("2. Lookup all your files");
            System.out.println("3. Lookup all public files of a user");
            System.out.println("4. Request a file");
            System.out.println("5. View unread messages");
            System.out.println("6. File upload");
            int option = scanner.nextInt();
            out.writeUTF(""+option);
            if(option<3){
                //List<Pair> clients =(List<Pair>) in.;
                String info= in.readUTF();
                System.out.println("From Server....\n"+info);
            }
            else if(option==3){
                System.out.println("Enter the username whose public files you want to see:");
                String uname=  scanner.next();
                out.writeUTF(uname);
                String info= in.readUTF();
                System.out.println("From Server....\n"+info);
            }

            //sending file
            else if(option==6) {
                File file = new File("Codes/Client/abcd.txt");
                FileInputStream fileInputStream = new FileInputStream(file);

//            long fileLength = file.length();

                out.writeUTF("fileName " + "abcd.txt" + " " + file.length());
                System.out.println("fileName " + "abcd.txt" + " " + file.length());
                out.flush();

                // break file into chunks
                int bytes = 0;
                byte[] buffer = new byte[512];
                int CHUNK = 0;
                while ((bytes = fileInputStream.read(buffer)) != -1) {

//            if(CHUNK % 10000 == 0) System.out.println("Chunk #"+CHUNK);
                    CHUNK++;

                    out.write(buffer, 0, bytes);
                    out.flush();

//                try {
//                    // ACK
//                    String msg = dataInputStreamFile.readUTF();
//                    if(!msg.equals("ACK"))
//                    {
//                        System.out.println("Dusername not receive ACK...");
//                        break;
//                    }
//
//                }catch (SocketTimeoutException socketTimeoutException){
//                    System.out.println("TIMEOUT");
//                    out.writeUTF("TIMEOUT "+fileType+" "+fileName);
//                    out.flush();
//                    fileInputStream.close();
//                    return;
//                }
                }
                fileInputStream.close();

                // send confirmation
                out.writeUTF("ACK");
                out.flush();

                String msg = in.readUTF();
                if (msg.equals("ACK")) {
                    System.out.println("File Upload Completed");

                } else System.out.println("File Upload Failed");
            }

        }
    }
}
