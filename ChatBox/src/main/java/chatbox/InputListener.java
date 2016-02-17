package chatbox;

import model.Data;
import model.Message;
import model.ServerAction;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class InputListener implements Runnable {
    List<MessageReceiveListener> messageListeners = new ArrayList();
    List<ServerActionReceiveListener> serverActionListeners = new ArrayList();
    private Socket socket;
    DataInputStream dis;

    public InputListener(Socket socket) throws IOException {
        this.socket = socket;
        dis = new DataInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        while (true){
            try {
                String input = dis.readUTF();
                Object receivedObject = Data.readObject(input);
                if (receivedObject instanceof Message){
                    Message receivedMessage = (Message) receivedObject;
                    onMessageReceivedEvent(receivedMessage);
                }
                else{
                    ServerAction receivedAction = (ServerAction) receivedObject;
                    onServerActionReceivedEvent(receivedAction);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onMessageReceivedEvent(Message message) {
        for (MessageReceiveListener listener : messageListeners) {
            listener.onMessageRecieve(message);
        }
    }

    public void addListener(MessageReceiveListener listener) {
        if (listener != null) {
            messageListeners.add(listener);
        }
    }

    private void onServerActionReceivedEvent(ServerAction serverAction) {
        for (ServerActionReceiveListener listener : serverActionListeners) {
            listener.onServerActionRecieve(serverAction);
        }
    }

    public void addServerActionListener(ServerActionReceiveListener listener) {
        if (listener != null) {
            serverActionListeners.add(listener);
        }
    }
}
