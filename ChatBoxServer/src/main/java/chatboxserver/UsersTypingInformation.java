package chatboxserver;

import model.Action;
import model.Data;
import model.ServerAction;
import model.User;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UsersTypingInformation extends Thread {

    public void run() {
        List<UserThread> userThreads = new ArrayList();
        userThreads.addAll(Repository.getUsers().values());

        List<User> users = new ArrayList();
        for (String typingUser : Repository.getUsersTyping()) {
            User user = new User(typingUser);
            users.add(user);
        }

        ServerAction serverAction = new ServerAction(Action.IsTyping);
        serverAction.setUsers(users);
        String serializedUsers = Data.writeServerAction(serverAction);

        for (UserThread userThread : userThreads) {
            try {
                Socket userSocket = userThread.getSocket();
                DataOutputStream dos = new DataOutputStream(userSocket.getOutputStream());
                dos.writeUTF(serializedUsers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
