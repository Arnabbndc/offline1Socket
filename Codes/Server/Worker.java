package Server;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

public class Worker extends Thread {
    
    public HashMap<String, Worker> workers;
    private String username;
    private int fileCnt;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private boolean isOnline;
    public Worker(Socket socket)
    {
        this.socket = socket;
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

    public boolean isOnline() {
        return isOnline;
    }
    public void setOnline(boolean status){
        isOnline=status;
    }
    boolean isAlreadyLoggedIn(String uname){
        if(workers.containsKey(uname) && workers.get(uname).isOnline()) return true;
        return false;
    }
    public void run()
    {
        // buffers
        try {
            out = new DataOutputStream(this.socket.getOutputStream());
            in = new DataInputStream(this.socket.getInputStream());

            while (true)
            {
                Thread.sleep(1);
//                Date date = new Date();
//                out.writeObject(date.toString());


                out.writeUTF("Give your username");
                String textFromClient = in.readUTF();
                System.out.println("Client username: "+textFromClient);
                String username = textFromClient;
                if(!isAlreadyLoggedIn(username)) {
                  //  Worker user = new Worker(username, socket);
                    this.username= username;
                    this.isOnline=true;
                    workers.put(username, this);
                    out.writeUTF("Username: "+username+" login successful");
                    System.out.println("Username: "+username+" login successful");

                    new File("Codes/Server/files/" + username + "/public").mkdirs();
                    new File("Codes/Server/files/" + username + "/private").mkdirs();
                    textFromClient = in.readUTF();

                System.out.println("Text from client (File) "+textFromClient);

                StringTokenizer stringTokenizer = new StringTokenizer(textFromClient," ");
                Vector<String> tokens = new Vector<>();

                while (stringTokenizer.hasMoreTokens())
                {
                    tokens.add(stringTokenizer.nextToken());
                }




                //-----------receive file-------------------
                if(tokens.elementAt(0).equals("fileName"))
                {
                    System.out.println("fileName : "+tokens.elementAt(1));
//                    int filesize = Integer.parseInt(tokens.elementAt(1));
                    String fileName = tokens.elementAt(1);
//                    String fileType = tokens.elementAt(3);
//                    int CHUNK_SIZE = Integer.parseInt(tokens.elementAt(4));

                    try
                    {
//                        connectionSocketFile.setSoTimeout(5000);
//                        boolean ok = recieveFile(fileName,fileType,filesize,curWorker.getId(),disFile,dosFile,CHUNK_SIZE);
//                        connectionSocketFile.setSoTimeout(0);
                        int bytes = 0;
                        FileOutputStream fileOutputStream = new FileOutputStream("Codes/Server/files/"+username+"/public/"+fileName);

                        try{
                            int size = Integer.parseInt(tokens.elementAt(2));     // read file size
                            byte[] buffer = new byte[512];
                            int CHUNK = 0;
                            // extra
//                            CUR_BUFFER_SIZE += CHUNK_SIZE;
                            while (size > 0) {

                                boolean ok;
                                try {
                                    ok = (bytes = in.read(buffer, 0, Math.min(buffer.length, size))) != -1;
                                }catch (SocketTimeoutException socketTimeoutException){
//                                    CUR_BUFFER_SIZE -= CHUNK_SIZE;
                                    fileOutputStream.close();
                                    System.out.println("FileOutputStream Closed");

                                }

//                                if(!ok) break;

                                if(CHUNK % 10000 == 0)System.out.println("Chunk #"+CHUNK);
                                CHUNK++;

//            if(CHUNK != 1) // hardcode to check file size difference
                                fileOutputStream.write(buffer,0,bytes);

                                size -= bytes;      // read upto file size
                                // send ACK
//            if(CHUNK <= 1) { // hardcode timeout
//                                out.writeUTF("ACK");
//                                out.flush();
//            }

                            }

//                            CUR_BUFFER_SIZE -= CHUNK_SIZE;
                            fileOutputStream.close();
                            System.out.println("FileOutputStream Closed");

                        }catch (Exception e)
                        {
                            System.out.println("Exception ... ");
//                            CUR_BUFFER_SIZE -= CHUNK_SIZE;
                            fileOutputStream.close();
                            System.out.println("FileOutputStream Closed");
                        }

                        // check confirmation and validate file size
                        String msg = in.readUTF();
                        if(msg.equals("ACK")){
//                            File file = new File("server "+fileName);
//                            if(file.length() != filesize)
//                            {
//                                System.out.println("File size mismatch");
//                                file.delete();
//                                return false;
//                            }
//                        }
//                        else
//                        {
//                            File file = new File("files/"+userID+"/"+fileType+"/"+fileName);
//                            file.delete();
//                            return false;
//                        }
//
//                        return true;
//
//                        if(ok)
//                        {
                            out.writeUTF("ACK");
                            System.out.println("File Upload Completed");
                        }
//                        else
//                        {
//                            File file = new File("files/"+out.getId()+"/"+fileType+"/"+fileName);
//                            System.out.println(file.delete());
//                            out.writeUTF("NOT_ACK");
//                            System.out.println("File Upload Failed");
//                        }

                    }
                    catch(Exception e)
                    {
//                        File file = new File("files/"+out.getId()+"/"+fileType+"/"+fileName);
//                        System.out.println(file.delete());
                        out.writeUTF("File Deleted");
                        System.err.println("Could not transfer file.");
                    }

                }
                //-------------- receive file finished
            }
                else {
                    out.writeUTF("You are already logged in");
                    out.writeUTF("Username: "+username+" login failed");
                    Thread.currentThread().interrupt(); // preserve the message
                    socket.close();
                    return;
                }
                }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
