package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class User {
    private String username;
    private int fileCnt;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private boolean isOnline;
    public User(String username, Socket socket) throws IOException {
        this.username= username;
        this.socket = socket;
        out= new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        fileCnt=0;
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
}
