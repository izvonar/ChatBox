package chatboxserver;

import model.User;

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
