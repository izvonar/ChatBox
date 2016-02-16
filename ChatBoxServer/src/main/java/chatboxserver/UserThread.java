package chatboxserver;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import model.Action;
import model.Data;
import model.Message;
import model.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class UserThread extends Thread {
    private User user;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public UserThread(User user, Socket socket) {
        this.user = user;
        this.socket = socket;
    }

    public void run() {
        try {
            dis = new DataInputStream(getSocket().getInputStream());
            dos = new DataOutputStream(getSocket().getOutputStream());
        }catch (IOException e) {
            e.printStackTrace();
        }

        while (true){
            try{
                String clientInput = dis.readUTF();
                Message clientMessage = Data.readMessage(clientInput);
                if (clientMessage.getAction() == Action.GlobalMessage){
                    SendMessage(clientInput);
                    System.out.println("----------MESSAGE----------");
                    System.out.println("Sender: " + clientMessage.getSender().getNickname());
                    System.out.println("Date: " + clientMessage.getDate());
                    System.out.println("Message: " + clientMessage.getMessage());
                    System.out.println("----------------------------");
                }
                else{
                    SendPrivateMessage(clientMessage);
                }
            } catch (IOException e) {
                System.out.println("User " + user.getNickname() + " disconnected!");
                Repository.removeUser(user);
                //TODO Notify other users
                break;
            }
        }
    }

    private void SendMessage(String clientMessage) {
        for (UserThread userThread : Repository.getUsers().values())
        {
            try {
                Socket userSocket = userThread.getSocket();
                DataOutputStream dos = new DataOutputStream(userSocket.getOutputStream());
                dos.writeUTF(clientMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void SendPrivateMessage(Message clientMessage) {
    }

    public Socket getSocket() {
        return socket;
    }
}
