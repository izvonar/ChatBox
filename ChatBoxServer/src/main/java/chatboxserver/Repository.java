package chatboxserver;

import model.User;
import java.net.Socket;
import java.util.*;

public class Repository {

    private static Map<String, UserThread> users;
    private static Set<String> usersTyping;

    public Repository() {
        users = new HashMap();
        usersTyping = new HashSet<>();
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
            sendUsersInformation();
            return true;
        }
        return  false;
    }

    public static void removeUser(User user)
    {
        users.remove(user.getNickname());
        removeUserTyping(user.getNickname());
        sendUsersInformation();
    }

    public static UserThread getUserThread(String nickname) {
        return users.get(nickname);
    }

    private static void sendUsersInformation() {
        new UsersInformation().start();
    }

    public static Set<String> getUsersTyping() {
        return usersTyping;
    }

    private static void sendUsersTypingInformation() {
        new UsersTypingInformation().start();
    }

    public static boolean addUserTyping(String user)
    {
        if (!getUsersTyping().contains(user))
        {
            getUsersTyping().add(user);
            sendUsersTypingInformation();
            return true;
        }
        return  false;
    }

    public static void removeUserTyping(String user)
    {
        getUsersTyping().remove(user);
        sendUsersTypingInformation();
    }
}
