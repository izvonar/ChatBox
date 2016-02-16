/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatbox;

import com.sun.org.apache.bcel.internal.generic.ACONST_NULL;
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

/**
 *
 * @author Ivan
 */
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
            System.out.println("Connecting...");
            DataInputStream dis;
            DataOutputStream dos;
            if (clientSocket.isConnected()) {
                System.out.println(clientSocket.getInetAddress().getHostName());

                dis = new DataInputStream(clientSocket.getInputStream());
                dos = new DataOutputStream(clientSocket.getOutputStream());
                User user = new User(nickname);
                ServerAction joinAction = new ServerAction(user, Action.UserJoin);
                String request = Data.writeServerAction(joinAction);
                dos.writeUTF(request);
                String response = dis.readUTF();
                ServerAction serverResponse = Data.readServerAction(response);
                System.out.println("Server response: " + response);
                System.out.println("ServerAction response: " + serverResponse.getAction());

                if (serverResponse.getAction() == Action.NicknameTaken) {
                    onStatusChangeEvent(Action.NicknameTaken);
                } else {
                    onStatusChangeEvent(Action.UserConnected);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
            onStatusChangeEvent(Action.ServerOffline);
        }
    }

    private void onStatusChangeEvent(Action status) {
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
