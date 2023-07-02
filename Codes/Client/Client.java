package Client;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;
import Server.Pair;
import Server.Server;

public class Client {

    public static void receiveFile(String username, String fileName, DataInputStream in, DataOutputStream out) throws IOException {
                int bytes = 0;
                FileOutputStream fileOutputStream = new FileOutputStream("Codes/Client/Downloads/" + username + "/" + fileName);
                int chunkSize= Integer.parseInt(in.readUTF());
                int size= Integer.parseInt(in.readUTF());
                    byte[] buffer = new byte[chunkSize];
                    while (size > 0) {
                        bytes = in.read(buffer, 0, Math.min(buffer.length, size)) ;
                        fileOutputStream.write(buffer, 0, bytes);
                        size -= bytes;
                    }
                String msg = in.readUTF();
                if (msg.equals("Complete")) {
                    System.out.println("File Downloading Completed");
                }

    }
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
        new File("Codes/Client/Downloads/"+username).mkdirs();

        
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
            System.out.println("7. Logout");
            int option = scanner.nextInt();
            if(option<4 || option==7)
                out.writeUTF(""+option);
            if(option<3){
                //List<Pair> clients =(List<Pair>) in.;
                String info= in.readUTF();
                System.out.println("From Server....\n"+info);
                if(option==2){
                    System.out.println("Want to download file from here?\n1. Yes\n2. No");
                    int choice= scanner.nextInt();
                    out.writeUTF(""+choice);
                    if(choice==2)continue;
                    System.out.println("Enter file name...");
                    String fileName = scanner.next();
                    out.writeUTF(fileName);
                    if(in.readUTF().equalsIgnoreCase("Wrong file")){
                        System.out.println("No such file exists");
                        continue;
                    }
                    System.out.println("File "+fileName+" download starts..");
                    receiveFile(username, fileName,in, out);
                }
            }
            else if(option==3){
                System.out.println("Enter the username whose public files you want to see:");
                String uname=  scanner.next();
                out.writeUTF(uname);
                String info= in.readUTF();
                System.out.println("From Server....\n"+info);
                System.out.println("Want to download?\n1. Yes\n2. No");
                int choice= scanner.nextInt();
                out.writeUTF(""+choice);
                if(choice==2)continue;
                System.out.println("Enter file name...");
                String fileName = scanner.next();
                out.writeUTF(fileName);
                if(in.readUTF().equalsIgnoreCase("Wrong file")){
                    System.out.println("No such file exists");
                    continue;
                }
                System.out.println("File "+fileName+" from user "+uname+" download starts..");
                receiveFile(username, fileName,in, out);
            }

            //sending file
            else if(option==6) {
                System.out.println("1. Public\n2. Private");
                int fileVisibility= scanner.nextInt();
                System.out.println("Enter File Name:");
                String fileName;
               fileName= scanner.next();
//                fileName= "abcd.txt";//temporary
                File file = new File("Codes/Client/files/"+fileName);
                if(!file.exists()){
                    System.out.println("File \""+fileName+"\" does not exist");
                    continue;
                }
                out.writeUTF(""+option);
                out.writeUTF(""+fileVisibility);
                out.writeUTF(fileName);
                out.writeUTF(""+file.length());
                FileInputStream fileInputStream = new FileInputStream(file);
                out.flush();

//            long fileLength = file.length();
                System.out.println("fileName " + fileName + " " + file.length());
                textFromServer= in.readUTF();
                if(textFromServer.equals("Not Ok")){
                    System.out.println("file can't be uploaded for buffer size issue");
                    continue;
                }
                int chunkSize= Integer.parseInt(textFromServer);
                int fileId = Integer.parseInt(in.readUTF());
                System.out.println("From server-- Chunk size: "+chunkSize+" FileId: "+fileId);
                // break file into chunks
                int bytes = 0;
                byte[] buffer = new byte[chunkSize];
                int CHUNK = 0;
                while ((bytes = fileInputStream.read(buffer)) != -1) {

            if(CHUNK % 10000 == 0) System.out.println("Chunk #"+CHUNK);
                    CHUNK++;

                    out.write(buffer, 0, bytes);
                    out.flush();

                try {
                    // ACK
                    String msg = in.readUTF();
                    if(!msg.equals("ACK"))
                    {
                        System.out.println("Not received ACK...");
                        break;
                    }

                }catch (SocketTimeoutException socketTimeoutException){
                    System.out.println("TIMEOUT");
                    //out.writeUTF("TIMEOUT "+fileType+" "+fileName);
                    out.flush();
                    fileInputStream.close();
                    return;
                }
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
            else if(option==7){
                System.out.println("Logging out. All your data will be saved...");
                socket.close();
                return;
            }

        }
    }
}
