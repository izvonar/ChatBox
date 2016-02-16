package chatbox;

import model.Action;
import model.Data;
import model.ServerAction;
import model.User;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectToServer extends Thread {

    private int port = 1234;
    private String address = "localhost";
    private String nickname;
    private Socket clientSocket;
    private List<StatusChangeListener> listeners = new ArrayList();

    public ConnectToServer(String nickname) {
        this.nickname = nickname;
    }

    public void run() {

        try {
            clientSocket = new Socket(address, port);
            DataInputStream dis;
            DataOutputStream dos;
            if (clientSocket.isConnected()) {
                dis = new DataInputStream(clientSocket.getInputStream());
                dos = new DataOutputStream(clientSocket.getOutputStream());
                User user = new User(nickname);
                ServerAction joinAction = new ServerAction(user, Action.UserJoin);
                String request = Data.writeServerAction(joinAction);
                dos.writeUTF(request);
                String response = dis.readUTF();
                onStatusChangeEvent(response);
            }
        } catch (IOException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
            onStatusChangeEvent(Action.ServerOffline.toString());
        }
    }

    private void onStatusChangeEvent(String status) {
        for (StatusChangeListener listener : listeners) {
            listener.onStatusChange(status);
        }
    }

    public void addListener(StatusChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
}
