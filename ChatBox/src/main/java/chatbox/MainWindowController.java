package chatbox;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Action;
import model.Data;
import model.Message;
import model.User;
import java.io.*;
import java.net.Socket;
import java.net.URL;
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
    private ListView<String> listUsers;

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
        dis = new DataInputStream(clientSocket.getInputStream());
        dos = new DataOutputStream(clientSocket.getOutputStream());
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

        anchorPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage(txtMessage.getText());
            }
        });

        listUsers.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                String item = listUsers.getSelectionModel().getSelectedItem();
                listUsers.getSelectionModel().clearSelection();
                privateChatWindow(clientSocket, item, this);
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

    private void startInputListener(Socket socket)
    {
        try {
            InputListener inputListener = new InputListener(socket);
            inputListener.addListener(message -> {
                displayMessage(message);
            });
            inputListener.addServerActionListener(serverAction -> {
               //TODO Server actions, receive users...
            });
            new Thread(inputListener).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayMessage(Message message) {
        Platform.runLater(() -> {
            Text text = new Text(message.getSender().getNickname() + ": " + message.getMessage());
            text.fontSmoothingTypeProperty().set(FontSmoothingType.GRAY);
            text.setFont(Font.font("Arial", 14));
            boxMessages.getChildren().add(text);
        });
    }





    private ObservableList<String> getUsers(String input, String nickname) {
        try {
            byte[] data = Base64.getDecoder().decode(input);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Object o = ois.readObject();
            ois.close();
            List<String> list = (List<String>) o;
            list.remove(nickname);
            ObservableList<String> names = FXCollections.observableArrayList(list);

            return names;

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return FXCollections.emptyObservableList();
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
