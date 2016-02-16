package chatboxserver;

import model.Action;
import model.Data;
import model.ServerAction;
import model.User;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class AddUser implements Runnable {

    private final User user;
    private final Repository repository;
    private final Socket socket;
    private DataOutputStream dos;

    public AddUser(User user, Repository repository, Socket socket) {

        this.user = user;
        this.repository = repository;
        this.socket = socket;
        try {
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String response;
        ServerAction userAction;
        if (repository.addUser(user, socket)) {
            userAction = new ServerAction(user, Action.UserConnected);
            response = Data.writeServerAction(userAction);
        }
        else{
            userAction = new ServerAction(user, Action.NicknameTaken);
            response = Data.writeServerAction(userAction);
        }
        try {
            dos.writeUTF(response);
        } catch (IOException e) {
            System.out.println("Error while sending response!, " + e.getMessage());
            e.printStackTrace();
        }
    }
}
