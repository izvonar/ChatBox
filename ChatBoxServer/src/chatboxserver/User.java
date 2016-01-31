/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatboxserver;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ivan
 */
class User extends Thread {

    private String nickname;
    private Socket clientSocket;
    DataInputStream dis;
    DataOutputStream dos;
    List<ClientStatusListener> listeners = new ArrayList();
    Map<String, User> users;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public User(String nickname, Socket clientSocket, Map<String, User> users) throws IOException {
        this.nickname = nickname;
        this.clientSocket = clientSocket;
        this.users = users;
        dis = new DataInputStream(clientSocket.getInputStream());
        dos = new DataOutputStream(clientSocket.getOutputStream());
    }

    public void addListener(ClientStatusListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    private void onStatusChangeEvent(String nickname) {
        for (ClientStatusListener listener : listeners) {
            listener.onStatusChange(nickname);
        }

        String allUsers;
        try {
            users.remove(nickname);
            allUsers = Write(users);
            SendToAll(allUsers, users, nickname, true);
        } catch (IOException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        String allUsers;
        try {
            allUsers = Write(users);
            SendToAll(allUsers, users, nickname, true);
        } catch (IOException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }

        while (true) {
            try {
                String inputMessage = dis.readUTF();
                System.out.println(nickname + ": " + inputMessage);

                String[] data = inputMessage.split(":");
                switch (data[0]) {
                    case "message@all":
                        SendToAll(data[1], users, nickname, false);
                        break;
                    case "private":
                        String target = data[1];
                        String message = data[2];
                        SendPrivateMessage(message, users, nickname, target);
                        break;
                    default:
                        System.out.println(data[0]);
                        break;
                }

                dos.writeUTF(inputMessage);
            } catch (IOException ex) {
                System.out.println(nickname + " disconnected!");
                onStatusChangeEvent(nickname);
                break;
            }
        }
    }

    private static void SendToAll(String message, Map<String, User> users, String sender, boolean userInfo) {
        final String format;
        if (userInfo) {
            format = "users:";
        } else {
            format = "message:" + sender + ":";
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (User user : users.values()) {
                    if (user.getNickname() != sender || userInfo == true) {
                        try {
                            DataOutputStream dos = new DataOutputStream(user.getClientSocket().getOutputStream());
                            dos.writeUTF(format + message);
                        } catch (IOException ex) {
                            Logger.getLogger(ChatBoxServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }).start();
    }

    private static void SendPrivateMessage(String message, Map<String, User> users, String sender, String target) {
        final String format = "private:" + sender + ":";
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (User user : users.values()) {
                    if (user.getNickname().equals(target)) {
                        try {
                            DataOutputStream dos = new DataOutputStream(user.getClientSocket().getOutputStream());
                            dos.writeUTF(format + message);
                            System.out.println("PRIVATE TO " + format + message);
                        } catch (IOException ex) {
                            Logger.getLogger(ChatBoxServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }).start();
    }

    private static String Write(Map<String, User> users) throws FileNotFoundException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(baos);
        List<String> list = new ArrayList();
        for (User user : users.values()) {
            list.add(user.getNickname());
        }

        stream.writeObject(list);
        stream.close();
        String ser = Base64.getEncoder().encodeToString(baos.toByteArray());
        //System.out.println(ser);
        return ser;
    }

}
