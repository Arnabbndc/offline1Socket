package Server;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Random;

public class Worker extends Thread {
    
    public HashMap<String, Worker> workers;
    private String username;
    private int fileCnt;
    private Socket socket;
    private Socket socketFile;
    private DataOutputStream out;
    private DataInputStream in;
    private DataOutputStream outFile;
    private DataInputStream inFile;
    private boolean isOnline;



    public Worker(Socket socket, Socket socketFile)
    {
        this.socket = socket;
        this.socketFile = socketFile;
        workers= Server.getWorkers();
        fileCnt=0;
        isOnline=false;
    }
    public DataOutputStream getDataOutputStream(){
        return out;
    }
    public DataInputStream getDataInputStream(){
        return in;
    }

    public String getUsername() {
        return username;
    }

    public boolean isOnline() {
        return isOnline;
    }
    public void setOnline(boolean status){
        isOnline=status;
    }
    public void setWorkers(HashMap<String, Worker> workers) {
        this.workers = workers;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFileCnt(int fileCnt) {
        this.fileCnt = fileCnt;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }
    boolean isLoggedIn(String uname){
        if(workers.containsKey(uname)&& workers.get(uname).isOnline() ) return true;
        return false;
    }
    boolean isPreviousUser(String uname){
        if(workers.containsKey(uname)){
            return true;
        }
        return false;
    }
    public void sendUserList() throws IOException {
        String info = "Users: \n";
        for (String key : workers.keySet()) {
            Worker worker = workers.get(key);
            info+="Username:  "+worker.getUsername()+" | isOnline: "+worker.isOnline+"\n";
        }
        info+="\n";
        out.writeUTF(info);

    }
    public void lookupFiles(String username ) throws IOException {
        File directory = new File("Codes/Server/files/" + username + "/public");

        String []files = directory.list();
        String info = "Public files of user \""+username+"\".... \n";

        for(String s: files){
            info+="\t"+s+"\n";
        }
        info+="\n";
        if(username.equalsIgnoreCase(this.username)){
            directory= new File("Codes/Server/files/" + username + "/private");
            files = directory.list();
            info+= "Private files of user \""+username+"\".... \n";
            for(String s: files){
                info+="\t"+s+"\n";
            }
            info+="\n";
        }
        out.writeUTF(info);
    }
    public long generateChunkSize(){
        long diff= Server.MAX_CHUNK_SIZE- Server.MIN_CHUNK_SIZE;
        Random random =  new Random();
        return Server.MIN_CHUNK_SIZE+Math.abs(random.nextInt()%diff);
    }
//    public String[] lookupPrivateFiles(int uID){
//        File directoryPath = new File("files/"+uID+"/private");
//        //List of all files and directories
//        String contents[] = directoryPath.list();
//        return contents;
//    }
    public int generateFileId(String uname, String fileName){
        String name= uname+"_"+fileName;
        if(!Server.fileIds.containsKey(name))
            Server.fileIds.put(name,Server.fileIds.size()+1);
        return Server.fileIds.get(name);
    }
    public static void receiveFile(String username,int chunkSize,String fileName, int fileSize, String fileVisibility, DataInputStream in, DataOutputStream out, DataInputStream inFile, DataOutputStream outFile) throws IOException {

//                        connectionSocketFile.setSoTimeout(5000);
//                        boolean ok = recieveFile(fileName,fileType,filesize,curWorker.getId(),disFile,dosFile,CHUNK_SIZE);
//                        connectionSocketFile.setSoTimeout(0);
            int bytes = 0, size=fileSize;
            FileOutputStream fileOutputStream = new FileOutputStream("Codes/Server/files/" + username + "/" + fileVisibility + "/" + fileName);

            try {
                byte[] buffer = new byte[chunkSize];
                int CHUNK = 0;
                // extra
                Server.CUR_BUFFER_SIZE += chunkSize;
                while (size > 0) {

                    try {
                        bytes = inFile.read(buffer, 0, Math.min(buffer.length, size));
                    } catch (SocketTimeoutException socketTimeoutException) {
                        Server.CUR_BUFFER_SIZE -= chunkSize;
                        System.out.println("TIMEOUT!!!");
                        System.out.println("Uploading Failed");
                        File file = new File("Codes/Server/files/" + username + "/" + fileVisibility + "/" + fileName);
                        file.delete();
                        out.writeUTF("TIMEOUT!!!\nFailed");
                        out.flush();
                        fileOutputStream.close();
                        return;

                    }
                    if(bytes==-1) break;
                    if(CHUNK % 1 == 0) System.out.println("Chunk #" + CHUNK+" bytes: "+bytes);
                    CHUNK++;
                    fileOutputStream.write(buffer, 0, bytes);
                    size -= bytes;
                    out.writeUTF("Acknowledge");
                    out.flush();

                }
                Server.CUR_BUFFER_SIZE -= chunkSize;
                fileOutputStream.close();
                if(in.readUTF().equals("Completed")){
                    File file = new File("Codes/Server/files/" + username + "/" + fileVisibility + "/" + fileName);
                    System.out.println(file.getName());
                    if(file.length() != fileSize)
                    {
                        System.out.println("File size doesn't match "+file.length()+" "+fileSize);
                        file.delete();
                        System.out.println("Uploading Failed");
                        out.writeUTF("Size mismatch!\nFailed");
                        return;
                    }
                    out.writeUTF("Completed");
                    System.out.println("File Upload Completed");
                }
                else
                {
                    File file = new File("Codes/Server/files/" + username + "/" + fileVisibility + "/" + fileName);

                    file.delete();
                    System.out.println("No Acknowledge received from user");
                    System.out.println("Uploading Failed");
                    out.writeUTF("No acknowledge received from user!\nFailed");
                    return;
                }

            }catch (Exception e)
            {
                System.out.println("Exception ... ");
                e.printStackTrace();
                Server.CUR_BUFFER_SIZE -= chunkSize;
                fileOutputStream.close();
                File file = new File("Codes/Server/files/" + username + "/" + fileVisibility + "/" + fileName);

                file.delete();
                System.out.println("Uploading Failed");
                out.writeUTF("Exception!\nFailed");
                return;

            }

    }

        public static void sendFile( File file, DataInputStream in, DataOutputStream out, DataInputStream inFile, DataOutputStream outFile) throws IOException {
        out.writeUTF(""+Server.MAX_CHUNK_SIZE);
        out.writeUTF(""+file.length());
        FileInputStream fileInputStream = new FileInputStream(file);
        out.flush();
        String fileName= file.getName();
        System.out.println("fileName " + fileName + " " + file.length());

        int bytes = 0;
        byte[] buffer = new byte[Server.MAX_CHUNK_SIZE];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            outFile.write(buffer, 0, bytes);
            outFile.flush();
        }
        fileInputStream.close();
        out.writeUTF("Complete");
        out.flush();
        System.out.println("File Download Completed");

    }
    public void run()
    {
        // buffers
        try {
            out = new DataOutputStream(this.socket.getOutputStream());
            in = new DataInputStream(this.socket.getInputStream());
            outFile= new DataOutputStream(this.socketFile.getOutputStream());
            inFile = new DataInputStream(this.socketFile.getInputStream());

            out.writeUTF("Give your username");
            String textFromClient = in.readUTF(), optionChosen;
            System.out.println("Client username: " + textFromClient);
            String username = textFromClient;
            if (!isLoggedIn(username)) {
                this.username = username;
                this.isOnline = true;
                out.writeUTF("Username: " + username + " login successful");
                System.out.println("Username: " + username + " login successful");
                if(!isPreviousUser(username)) {

                    new File("Codes/Server/files/" + username + "/public").mkdirs();
                    new File("Codes/Server/files/" + username + "/private").mkdirs();
                    new File("Codes/Server/files/" + username + "/Messages.txt").createNewFile();

                }
//                else{
//                    workers.get(username).setOnline(true);
//                }
                workers.put(username, this);
                while (true) {
                    Thread.sleep(1);
//                Date date = new Date();
//                out.writeObject(date.toString());
                    //  Worker user = new Worker(username, socket);
                    while(true) {
                        textFromClient = in.readUTF();
                        String []option= textFromClient.split(" ");
                        if(option.length==2 && option[0].equals("Option")){
                            int opt=0;
                            try {
                                opt=Integer.parseInt(option[1]);
                                if(opt>=1 && opt<=7){
                                    optionChosen=option[1];
                                    break;
                                }
                            }catch (NumberFormatException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    System.out.println("Option selected by user \""+username+"\": " + optionChosen);
//                    StringTokenizer stringTokenizer = new StringTokenizer(textFromClient, " ");
//                    Vector<String> tokens = new Vector<>();
//
//                    while (stringTokenizer.hasMoreTokens()) {
//                        tokens.add(stringTokenizer.nextToken());
//                    }

                    if(optionChosen.equals("1")) {
                        sendUserList();
                    }
                    else if(optionChosen.equals("2")) {
                        lookupFiles(this.username);
                        if(in.readUTF().equals("2"))continue;
                        String fileName = in.readUTF();
                        File file= new File("Codes/Server/files/" + username + "/public/"+fileName);
                        if(!file.exists()) file= new File("Codes/Server/files/" + username + "/private/"+fileName);
                        if(!file.exists()) {
                            out.writeUTF("Wrong file");
                            System.out.println("No such file \""+fileName+"\" exists\nDownloading failed..");
                            continue;
                        }
                        else{
                            out.writeUTF("Valid file");
                            System.out.println("File "+fileName+" is going to be downloaded by user "+username);
                            sendFile(file, in, out,inFile,outFile);
                        }
                    }
                    else if(optionChosen.equals("3")) {
                        String uname= in.readUTF();
                        lookupFiles(uname);
                        if(in.readUTF().equals("2"))continue;
                        String fileName = in.readUTF();
                        File file= new File("Codes/Server/files/" + uname + "/public/"+fileName);
                        if(!file.exists()) {
                            out.writeUTF("Wrong file");
                            System.out.println("No such file \""+fileName+"\" exists\nDownloading failed..");
                            continue;
                        }
                        else{
                            out.writeUTF("Valid file");
                            System.out.println("File "+fileName+" is going to be downloaded by user "+username);
                            sendFile(file, in, out, inFile, outFile);
                        }

                    }
                    else if (optionChosen.equals("4")) {
                        String description = in.readUTF();
                        String reqId= in.readUTF();
                        System.out.println("User "+username+" requested a file with description \""+description+"\" and RequestID: "+reqId+"\nBroadcasting it to all users....");
                        for (String key : workers.keySet()) {
                            if(!key.equals(username)) {
                                FileOutputStream fos = new FileOutputStream("Codes/Server/files/" + key + "/Messages.txt", true);
                                String msg = "FileRequestedBy: "+username+" RequestID: "+reqId+" Description: "+description+"\n";
                                fos.write(msg.getBytes());
                            }
                        }

                    }
                        //-----------receive file-------------

                   else if (optionChosen.equals("6")) {
                        String fileVisibility = in.readUTF();
                        if (fileVisibility.equals("1")) fileVisibility = "public";
                        else fileVisibility = "private";
                        String fileName = in.readUTF();
                        int size = Integer.parseInt(in.readUTF());
                        int fileSize = size;
                        System.out.println("User \"" + username + "\" wants to upload file \"" + fileName + "\" as a " + fileVisibility + " file having file length " + size);
                        if (Server.CUR_BUFFER_SIZE + size <= Server.MAX_BUFFER_SIZE) {
                            // int filesize = Integer.parseInt(tokens.elementAt(1));
                            System.out.println("Does not exceed buffer size.\nReceiving file \"" + fileName + "\"....");
                            int chunkSize = (int) generateChunkSize();
                            int fileId = generateFileId(username, fileName);
                            out.writeUTF("" + chunkSize);
                            out.writeUTF("" + fileId);
//                    String fileType = tokens.elementAt(3);
//                    int CHUNK_SIZE = Integer.parseInt(tokens.elementAt(4));
                            socketFile.setSoTimeout(3000);
                            try {
                                receiveFile(username, chunkSize, fileName, fileSize, fileVisibility, in, out, inFile, outFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            socketFile.setSoTimeout(0);


                        }
                    }
                    //-------------- receive file finished
                    else if(optionChosen.equals("7")){
                        System.out.println("User " + username + " is logging out");
                        this.isOnline=false;
                        Thread.currentThread().interrupt(); // preserve the message
                        socket.close();
                        return;
                    }

                }
            } else {
                out.writeUTF("You are already logged in");
                System.out.println("Username: " + username + " login failed since already logged in");
                Thread.currentThread().interrupt(); // preserve the message
                socket.close();
                return;
            }
        }

            catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
