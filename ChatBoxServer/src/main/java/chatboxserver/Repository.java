package chatboxserver;

import model.User;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Repository {

    private static Map<String, UserThread> users;

    public Repository() {
        users = new HashMap<>();
    }

    public static Map<String, UserThread> getUsers() {
        return users;
    }

    public boolean addUser(User user, Socket socket)
    {
        if (users.get(user.getNickname()) == null)
        {
            UserThread userThread = new UserThread(user, socket);
            userThread.start();
            users.put(user.getNickname(), userThread);
            return true;
        }
        return  false;
    }

    public static void removeUser(User user)
    {
        users.remove(user.getNickname());
    }

    public static UserThread getUserThread(String nickname) {
        return users.get(nickname);
    }
}
