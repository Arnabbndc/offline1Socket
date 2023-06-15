package Server;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

public class Worker extends Thread {
    Socket socket;

    public Worker(Socket socket)
    {
        this.socket = socket;
    }

    public void run()
    {
        // buffers
        try {
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            DataInputStream in = new DataInputStream(this.socket.getInputStream());

            while (true)
            {
                Thread.sleep(1);
//                Date date = new Date();
//                out.writeObject(date.toString());
                String textFromClient = in.readUTF();
                System.out.println("Text from client (File) "+textFromClient);

                StringTokenizer stringTokenizer = new StringTokenizer(textFromClient," ");
                Vector<String> tokens = new Vector<>();

                while (stringTokenizer.hasMoreTokens())
                {
                    tokens.add(stringTokenizer.nextToken());
                }

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
//                        boolean ok = recieveFile(fileName,fileType,filesize,curUser.getId(),disFile,dosFile,CHUNK_SIZE);
//                        connectionSocketFile.setSoTimeout(0);
                        int bytes = 0;
                        FileOutputStream fileOutputStream = new FileOutputStream("Codes/Server/server "+fileName);

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
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
