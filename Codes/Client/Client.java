package Client;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;


public class Client {
    public static String generateRequestId(String uname, String description, HashMap<String, String> reqIds){
        String name= uname+"_"+description;
        if(!reqIds.containsKey(name))
            reqIds.put(name,uname+"_"+(reqIds.size()+1));
        return reqIds.get(name);
    }
    public static void sendFile(File file,int chunkSize, DataInputStream in, DataOutputStream out, DataInputStream inFile, DataOutputStream outFile) throws IOException {

        FileInputStream fileInputStream = new FileInputStream(file);
        out.flush();
        int bytes = 0;
        byte[] buffer = new byte[chunkSize];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            outFile.write(buffer, 0, bytes);
            outFile.flush();
            try {
                String msg = in.readUTF();
                if(!msg.equals("Acknowledge"))
                {
                    System.out.println("Not received Acknowledge...");
                    break;
                }

            }catch (SocketTimeoutException socketTimeoutException){
                System.out.println("TIMEOUT");
                out.flush();
                fileInputStream.close();
                return;
            }

        }
        fileInputStream.close();
        out.writeUTF("Completed");
        String msg = in.readUTF();
        if (msg.equals("Completed")) {
            System.out.println("File Upload Completed");

        } else {
            System.out.println("From Server: "+msg );
            System.out.println("File Upload Failed");
        }
    }

    public static void receiveFile(String username, String fileName, DataInputStream in, DataOutputStream out, DataInputStream inFile, DataOutputStream outFile) throws IOException {
                int bytes = 0;
                FileOutputStream fileOutputStream = new FileOutputStream("Codes/Client/Downloads/" + username + "/" + fileName);
                int chunkSize= Integer.parseInt(in.readUTF());
                int size= Integer.parseInt(in.readUTF());
                    byte[] buffer = new byte[chunkSize];
                    while (size > 0) {
                        bytes = inFile.read(buffer, 0, Math.min(buffer.length, size)) ;
                        fileOutputStream.write(buffer, 0, bytes);
                        size -= bytes;
                    }
                String msg = in.readUTF();
                if (msg.equals("Complete")) {
                    System.out.println("File Downloading Completed");
                }

    }

    public static void main(String[] args) throws IOException {
        HashMap<String, String> reqIds= new HashMap<>();
        Socket socket = new Socket("localhost", 6667);
        Socket socketFile= new Socket("localhost", 7777);
        System.out.println("Connection established");
        System.out.println("Remote port: " + socket.getPort());
        System.out.println("Local port: " + socket.getLocalPort());
        System.out.println("Remote port for file: " + socketFile.getPort());
        System.out.println("Local port for file: " + socketFile.getLocalPort());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream outFile = new DataOutputStream(socketFile.getOutputStream());
        DataInputStream inFile = new DataInputStream(socketFile.getInputStream());
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

        

        while(true) {
            System.out.println("Console ...");
            System.out.println("Choose an option between 1-7");
            System.out.println("1. Lookup all clients");
            System.out.println("2. Lookup all your files");
            System.out.println("3. Lookup all public files of a user");
            System.out.println("4. Request a file");
            System.out.println("5. View unread messages");
            System.out.println("6. File upload");
            System.out.println("7. Logout");
            int option = scanner.nextInt();
            if(option<=5 || option==7)
                out.writeUTF("Option "+option);
            if(option<3|| option==5){
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
                    receiveFile(username, fileName,in, out, inFile, outFile);
                }
                if(option==5){
                    System.out.println("Want to upload a requested file?\n1. Yes\n2. No");
                    int decision= scanner.nextInt();
                    if(decision==1){
                        System.out.println("Enter RequestID:");
                        String reqId= scanner.next();
                        System.out.println("Enter File Name:");
                        String fileName= scanner.next();
                        File file = new File("Codes/Client/files/"+fileName);
                        if(!file.exists()){
                            System.out.println("File \""+fileName+"\" does not exist");
                            out.writeUTF("Done");
                            continue;
                        }
                        out.writeUTF("Uploading");
                        out.writeUTF(reqId);
                        out.writeUTF(fileName);
                        out.writeUTF(""+file.length());
                        System.out.println("fileName " + fileName + " " + file.length());
                        textFromServer= in.readUTF();
                        if(textFromServer.equals("Not Ok")){
                            System.out.println("file can't be uploaded for buffer size issue");
                            return;
                        }
                        int chunkSize= Integer.parseInt(textFromServer);
                        int fileId = Integer.parseInt(in.readUTF());
                        System.out.println("From server-- Chunk size: "+chunkSize+" FileId: "+fileId);
                        socketFile.setSoTimeout(30000);
                        sendFile(file,chunkSize,in,out, inFile, outFile);
                        socketFile.setSoTimeout(0);

                    }
                    else{
                        out.writeUTF("Done");
                    }
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
                receiveFile(username, fileName,in, out, inFile, outFile);
            }
            else if(option==4){
                System.out.println("Give file description ");
                String description = scanner.next();
                String reqId= generateRequestId(username,description,reqIds);
                System.out.println("Description of requested file: "+description+"\nRequest ID: "+reqId);
                out.writeUTF(description);
                out.flush();
                out.writeUTF(reqId);
            }
            else if(option==6) {
                System.out.println("Enter File Name:");
                String fileName= scanner.next();
                File file = new File("Codes/Client/files/"+fileName);
                if(!file.exists()){
                    System.out.println("File \""+fileName+"\" does not exist");
                    continue;
                }
                out.writeUTF("Option "+option);
                System.out.println("Choose visibility:\n1. Public\n2. Private");
                int fileVisibility= scanner.nextInt();
                out.writeUTF(""+fileVisibility);
                out.writeUTF(fileName);
                out.writeUTF(""+file.length());
                System.out.println("fileName " + fileName + " " + file.length());
                textFromServer= in.readUTF();
                if(textFromServer.equals("Not Ok")){
                    System.out.println("file can't be uploaded for buffer size issue");
                    return;
                }
                int chunkSize= Integer.parseInt(textFromServer);
                int fileId = Integer.parseInt(in.readUTF());
                System.out.println("From server-- Chunk size: "+chunkSize+" FileId: "+fileId);
                socketFile.setSoTimeout(30000);
                sendFile(file,chunkSize,in,out, inFile, outFile);
                socketFile.setSoTimeout(0);

            }
            else if(option==7){
                System.out.println("Logging out. All your data will be saved...");
                socket.close();
                socketFile.close();
                return;
            }

        }
    }
}
