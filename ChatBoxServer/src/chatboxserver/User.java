/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatboxserver;

import java.net.Socket;

/**
 *
 * @author Ivan
 */
class User extends Thread {

    private String nickname;
    private Socket clientSocket;

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

    public User(String nickname, Socket clientSocket) {
        this.nickname = nickname;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {

    }
}
