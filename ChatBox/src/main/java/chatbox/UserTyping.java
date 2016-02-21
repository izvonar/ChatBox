package chatbox;

import model.Action;
import model.Data;
import model.ServerAction;
import model.User;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class UserTyping extends Thread {
    private int port = 1234;
    private String address = "localhost";
    private Socket socket;
    private User user;
    private Action action;

    public UserTyping(User user, Action action) {
        this.user = user;
        this.action = action;
    }

    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket.isConnected()){
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                ServerAction serverAction = new ServerAction(user, action);
                String output = Data.writeServerAction(serverAction);
                dos.writeUTF(output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
