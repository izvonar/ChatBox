package chatbox;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MainWindowController implements Initializable {

    User user;
    DataInputStream dis;
    DataOutputStream dos;
    List<StatusChangeListener> listeners = new ArrayList();
    Timer keyTypedTime;
    long stoppedTypingDelay = 1000;
    boolean isTimerRunning = false;
    boolean enterKeyPressed = false;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private BorderPane borderPane;
    @FXML
    private Label buttonExit;
    @FXML
    private Label lblConnectionStatus;
    @FXML
    private Label lblUsersTyping;
    @FXML
    private Button btnSend;
    @FXML
    private TextField txtMessage;
    @FXML
    private VBox listUsers;
    @FXML
    private VBox boxMessages;
    @FXML
    private ScrollPane scrollPaneMessages;
    @FXML
    private Text txtTitle;
    private double originX;
    private double originY;
    private Socket clientSocket;

    public MainWindowController(Socket socket, User user) throws IOException {
        this.clientSocket = socket;
        this.user = user;
        if (socket.isConnected()) {
            dis = new DataInputStream(clientSocket.getInputStream());
            dos = new DataOutputStream(clientSocket.getOutputStream());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        borderPane.setOnMousePressed((MouseEvent event) -> {
            originX = event.getSceneX();
            originY = event.getSceneY();
            borderPane.requestFocus();
        });

        borderPane.setOnMouseDragged((MouseEvent event) -> {
            Stage primaryStage = (Stage) borderPane.getScene().getWindow();
            double xD = event.getScreenX() - originX;
            double yD = event.getScreenY() - originY;
            primaryStage.setX(xD);
            primaryStage.setY(yD);
        });

        buttonExit.setOnMousePressed((MouseEvent event) -> {
            Platform.exit();
        });

        borderPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage(txtMessage.getText());
            }
        });

        listUsers.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
//                String item = listUsers.getSelectionModel().getSelectedItem();
//                listUsers.getSelectionModel().clearSelection();
//                privateChatWindow(clientSocket, item, this);
            }
        });

        txtMessage.setOnKeyPressed(event -> {
            enterKeyPressed = false;
            if (event.getCode() == KeyCode.ENTER) {
                enterKeyPressed = true;
            }
        });

        txtMessage.setOnKeyTyped(event -> {
            if (enterKeyPressed) {
                return;
            }
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    new UserTyping(user, Action.StoppedTyping).start();
                    isTimerRunning = false;
                }
            };
            if (!isTimerRunning) {
                startTimer(timerTask);
            } else {
                restartTimer(timerTask);
            }
        });



        txtTitle.setText(txtTitle.getText() + " " + user.getNickname());
        startInputListener(clientSocket);
    }

    private void startTimer(TimerTask timerTask) {
        new UserTyping(user, Action.IsTyping).start();
        keyTypedTime = new Timer(true);
        keyTypedTime.schedule(timerTask, stoppedTypingDelay);
        isTimerRunning = true;
    }

    private void restartTimer(TimerTask timerTask) {
        keyTypedTime.cancel();
        keyTypedTime = new Timer(true);
        keyTypedTime.schedule(timerTask, stoppedTypingDelay);
    }

    private void stopTimer() {
        isTimerRunning = false;
        keyTypedTime.cancel();
        new UserTyping(user, Action.StoppedTyping).start();
    }

    private void onStatusChangeEvent(String status) {
        for (StatusChangeListener listener : listeners) {
            listener.onStatusChange(status);
        }
    }

    public void addListener(StatusChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @FXML
    void btnSendMessage() {
        sendMessage(txtMessage.getText());
    }

    private void sendMessage(String message) {
        stopTimer();
        Message newMessage = new Message(user, message, Action.GlobalMessage);
        String serializedMessage = Data.writeMessage(newMessage);
        try {
            dos.writeUTF(serializedMessage);
        } catch (IOException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
        txtMessage.clear();
    }

    private void startInputListener(Socket socket) {
        try {
            InputListener inputListener = new InputListener(socket);
            inputListener.addListener(message -> {
                displayMessage(message);
            });
            inputListener.addServerActionListener(serverAction -> {
                if (serverAction.getAction() == Action.UsersList) {
                    addUsers(serverAction);
                } else if (serverAction.getAction() == Action.IsTyping) {
                    showTypingUsers(serverAction);
                }
            });
            Thread listener = new Thread(inputListener);
            listener.setDaemon(true);
            listener.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void showTypingUsers(ServerAction typingUsers) {
        Platform.runLater(() -> {
            lblUsersTyping.setText("");
            List<User> listUsers = new ArrayList<User>();
            listUsers.addAll(typingUsers.getUsers());
            for (User u : listUsers) {
                if (u.getNickname().equals(this.user.getNickname())) {
                    typingUsers.getUsers().remove(u);
                }
            }
            if (typingUsers.getUsers().size() == 0) {
                return;
            }
            String ending = " are typing...";
            if (typingUsers.getUsers().size() == 1) {
                ending = " is typing...";
            }
            StringBuilder sb = new StringBuilder();
            for (User u : typingUsers.getUsers()) {
                sb.append(u.getNickname());
                sb.append(", ");

            }
            String users = sb.substring(0, sb.length() - 2);
            lblUsersTyping.setText(users + ending);
        });
    }

    private void displayMessage(Message message) {
        Platform.runLater(() -> {
            Node messageBubble = createMessageBubble(message);
            boxMessages.getChildren().add(messageBubble);
            startAnimation(messageBubble, 300);
        });

    }

    private void startAnimation(Node node, double duration) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }

    private void createUserPane(User user) {
        Pane pane = new Pane();
        pane.setCursor(Cursor.HAND);
        pane.setMinHeight(38);
        pane.setMinWidth(210);
        pane.getStyleClass().add("paneUser");
        HBox hBox = new HBox();
        hBox.setMaxWidth(210);
        hBox.setPrefHeight(30);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 0, 0, 20));
        Label username = new Label(user.getNickname());
        username.setTextFill(Color.WHITE);
        hBox.getChildren().add(username);
        pane.getChildren().add(hBox);
        pane.setOnMouseDragOver(event -> {
            username.setTextFill(Color.RED);

        });
        listUsers.getChildren().add(pane);
    }

    private void addUsers(ServerAction usersAction) {
        Platform.runLater(() -> {
            listUsers.getChildren().clear();
            for (User u : usersAction.getUsers()) {
                if (!u.getNickname().equals(user.getNickname())) {
                    createUserPane(u);
                }
            }
        });
    }

    private Node createMessageBubble(Message message) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String time = timeFormat.format(message.getDate());
        String bubbleType = "messageBubbleOther";
        Pos messageAligment = Pos.CENTER_LEFT;
        Color textColor = Color.BLACK;
        Text messageText = new Text(message.getMessage());
        Text messageTime = new Text(time);
        HBox root = new HBox();
        root.setId(message.getSender().getNickname());
        Pane messageBubble = new Pane();
        double bubbleWidth = messageText.getLayoutBounds().getWidth() + 20;
        if (bubbleWidth >= 200) {
            messageText.setWrappingWidth(200);
        }
        if (message.getSender().getNickname().equals(this.user.getNickname())) {
            messageAligment = Pos.CENTER_RIGHT;
            textColor = Color.WHITE;
            bubbleType = "messageBubble";
            root.getChildren().add(messageBubble);
            root.getChildren().add(messageTime);
            boxMessages.setMargin(root, new Insets(1, 0, 0, 0));
        } else {
            VBox vbox = new VBox();
            root.setMargin(messageTime, new Insets(0, 8, 0, 0));
            boxMessages.setMargin(root, new Insets(1, 0, 0, 0));
            Text sender = new Text(" " + message.getSender().getNickname());
            sender.setFont(Font.font(10.5));
            sender.setFill(Color.valueOf("#9197a3"));
            if (boxMessages.getChildren().size() == 0 || !boxMessages.getChildren().get(boxMessages.getChildren().size() - 1).getId().equals(message.getSender().getNickname())) {
                vbox.getChildren().add(sender);
                root.setMargin(messageTime, new Insets(13, 8, 0, 0));
                boxMessages.setMargin(root, new Insets(8, 0, 0, 0));
            }
            vbox.getChildren().add(messageBubble);
            root.getChildren().add(messageTime);
            root.getChildren().add(vbox);
        }
        root.setFillHeight(true);
        root.setAlignment(messageAligment);
        root.setMargin(messageBubble, new Insets(0, 8, 0, 8));
        messageBubble.getStyleClass().add(bubbleType);
        messageText.setFill(textColor);
        messageText.setFont(Font.font(15));
        messageTime.setFont(Font.font(10));
        HBox textContainer = new HBox(messageText);
        textContainer.setPadding(new Insets(3.6, 9, 3.6, 9));
        textContainer.setFillHeight(true);
        messageBubble.getChildren().add(textContainer);
        return root;
    }

    private void privateChatWindow(Socket socket, String target, MainWindowController mainCtrl) {

    }

    private void sendPrivateMessage(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
