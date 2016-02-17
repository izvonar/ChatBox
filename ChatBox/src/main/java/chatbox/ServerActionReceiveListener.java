package chatbox;


import model.ServerAction;

public interface ServerActionReceiveListener {
    void onServerActionRecieve(ServerAction serverAction);
}
