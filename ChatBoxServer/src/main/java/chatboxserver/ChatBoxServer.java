package chatboxserver;


import model.User;

public class ChatBoxServer {

    private static int port = 1234;

    public static void main(String[] args) {
        System.out.println("CHATBOX SERVER");
        Repository repo = new Repository();

        ConnectionThread connectionThread = new ConnectionThread(port, repo);
        connectionThread.start();
    }
}
