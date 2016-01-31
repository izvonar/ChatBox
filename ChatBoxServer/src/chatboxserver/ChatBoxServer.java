/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatboxserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ivan
 */
public class ChatBoxServer {

    public static void main(String[] args) {
        Socket socket;
        ServerSocket serverSocket;
        DataInputStream dis;
        DataOutputStream dos;

        Map<String, User> users = new HashMap<>();

        try {
            serverSocket = new ServerSocket(1234);
            System.out.println("Server started...");
        } catch (IOException ex) {
            System.out.println("Server error: " + ex.getMessage());
            return;
        }

        while (true) {
            try {
                socket = serverSocket.accept();

                if (socket.isConnected()) {
                    dis = new DataInputStream(socket.getInputStream());
                    dos = new DataOutputStream(socket.getOutputStream());
                    String message = dis.readUTF();
                    System.out.println("CLINET: " + message);

                    String[] data = message.split(":");
                    String response = "";
                    switch (data[0]) {
                        case "join":
                            if (checkNickname(data[1], users)) {
                                User u = new User(data[1], socket);
                                u.start();
                                users.put(data[1], u);
                                response = "server:connected";
                            } else {
                                response = "server:nickname-error";
                            }
                            break;
                    }
                    dos.writeUTF(response);
                    System.out.println(" ->SERVER: " + response);
                }
            } catch (IOException ex) {
                System.out.println("Server IOException: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("Server Exception: " + ex.getMessage());
                Logger.getLogger(ChatBoxServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static boolean checkNickname(String nickname, Map<String, User> users) {
        if (users.get(nickname) == null) {
            return true;
        }
        return false;
    }
}
