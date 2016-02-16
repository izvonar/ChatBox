package chatboxserver;

import model.User;
import java.util.HashMap;
import java.util.Map;

public class Repository {

    Map<String, User> users;

    public Repository() {
        users = new HashMap<>();
    }

    public boolean addUser(User user)
    {
        if (users.get(user.getNickname()) == null)
        {
            users.put(user.getNickname(), user);
            return true;
        }
        return  false;
    }

    public void removeUser(User user)
    {
        users.remove(user.getNickname());
    }
}
