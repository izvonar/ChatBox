package chatboxserver;

import model.Action;
import model.Data;
import model.ServerAction;
import model.User;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class RemoveUser implements Runnable {

    private final User user;
    private final Repository repository;

    public RemoveUser(User user, Repository repository) {

        this.user = user;
        this.repository = repository;
    }

    @Override
    public void run() {
        repository.removeUser(user);
    }
}
