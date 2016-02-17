package chatbox;

import model.Message;

public interface MessageReceiveListener {
    void onMessageRecieve(Message message);
}
