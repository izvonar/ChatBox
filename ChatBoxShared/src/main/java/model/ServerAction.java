package model;

import java.io.Serializable;

public class ServerAction implements Serializable {
    private Action action;
    private User user;

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

    public ServerAction(User user, Action action) {

        this.user = user;
        this.action = action;
    }
}
