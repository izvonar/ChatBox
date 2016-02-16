package chatboxserver;

import model.Data;
import model.ServerAction;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ConnectionThread extends Thread {

    private Socket socket;
    private ServerSocket serverSocket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Repository repository;

    public ConnectionThread(int port, Repository repository) {
        this.repository = repository;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("server started...");
        } catch (IOException e) {
            System.out.println("Error while starting server!, " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                socket = serverSocket.accept();
                if (socket.isConnected()) {
                    dis = new DataInputStream(socket.getInputStream());
                    String clientInput = dis.readUTF();
                    ServerAction clientAction = Data.readServerAction(clientInput);
                    switch (clientAction.getAction()) {
                        case UserJoin:
                            new Thread(new AddUser(clientAction.getUser(), repository, socket)).start();
                            break;
                        case UserDisconnect:
                            new Thread(new RemoveUser(clientAction.getUser(), repository)).start();
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
