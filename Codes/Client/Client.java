package Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("localhost", 6666);
        System.out.println("Connection established");
        System.out.println("Remote port: " + socket.getPort());
        System.out.println("Local port: " + socket.getLocalPort());

        // buffers
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());

        //
        while(true) {
//            String msg = (String) in.readObject();
//            System.out.println(msg);
            String textFromServer =  in.readUTF();
            System.out.println("Text from server: "+textFromServer);
            Scanner scanner= new Scanner(System.in);
            int id= scanner.nextInt();
            out.writeUTF(""+id);





            //sending file
            File file = new File("Codes/Client/abcd.txt");
            FileInputStream fileInputStream = new FileInputStream(file);

//            long fileLength = file.length();

            out.writeUTF("fileName "+ "abcd.txt"+" "+file.length());
            System.out.println("fileName "+  "abcd.txt"+" "+file.length());
            out.flush();

            // break file into chunks
            int bytes = 0;
            byte[] buffer = new byte[512];
            int CHUNK = 0;
            while ((bytes=fileInputStream.read(buffer))!=-1){

//            if(CHUNK % 10000 == 0) System.out.println("Chunk #"+CHUNK);
                CHUNK++;

                out.write(buffer,0,bytes);
                out.flush();

//                try {
//                    // ACK
//                    String msg = dataInputStreamFile.readUTF();
//                    if(!msg.equals("ACK"))
//                    {
//                        System.out.println("Did not receive ACK...");
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
            if(msg.equals("ACK")) {System.out.println("File Upload Completed");
                return;}
            else System.out.println("File Upload Failed");

        }
    }
}
