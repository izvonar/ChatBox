/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatbox;

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

                dos.writeUTF("join:" + nickname);
                String response = dis.readUTF();
                System.out.println("SERVER: " + response);

                if (response.equals("server:nickname-error")) {
                    onStatusChangeEvent("Nickname already in use!");
                } else {
                    onStatusChangeEvent("Connected!");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
            onStatusChangeEvent("ChatBox server is offline!");
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
