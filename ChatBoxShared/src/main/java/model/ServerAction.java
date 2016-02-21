package model;

import java.io.Serializable;
import java.util.List;

public class ServerAction implements Serializable {
    private Action action;
    private User user;
    private List<User> users = null;

    public ServerAction(User user, Action action) {

        this.user = user;
        this.action = action;
    }

    public ServerAction(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
