package chatbox;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.*;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MainWindowController implements Initializable {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private BorderPane borderPane;

    @FXML
    private Label buttonExit;

    @FXML
    private Label lblConnectionStatus;

    @FXML
    private Button btnSend;

    @FXML
    private TextField txtMessage;

    @FXML
    private VBox listUsers;

    @FXML
    private VBox boxMessages;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Text txtTitle;

    private double originX;
    private double originY;
    User user;
    private Socket clientSocket;
    DataInputStream dis;
    DataOutputStream dos;
    List<StatusChangeListener> listeners = new ArrayList();

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
            System.exit(0);
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
        txtTitle.setText(txtTitle.getText() + " " + user.getNickname());
        startInputListener(clientSocket);
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
                }
            });
            new Thread(inputListener).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayMessage(Message message) {
        Platform.runLater(() -> {
            Text text = new Text(message.getSender().getNickname() + ": " + message.getMessage());
            text.setFont(Font.font("Arial", 14));
            boxMessages.getChildren().add(text);
        });
    }

    private void createUserPane(User user) {
        Pane pane = new Pane();
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
