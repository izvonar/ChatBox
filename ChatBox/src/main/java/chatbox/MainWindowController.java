package chatbox;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Action;


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
    String nickname;
    private Socket clientSocket;
    DataInputStream dis;
    DataOutputStream dos;
    List<StatusChangeListener> listeners = new ArrayList();

    public MainWindowController(Socket socket, String nickname) throws IOException {
        this.clientSocket = socket;
        this.nickname = nickname;
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

        anchorPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    sendMessage(txtMessage.getText());
                }
            }
        });

        listUsers.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                String item = listUsers.getSelectionModel().getSelectedItem();
                listUsers.getSelectionModel().clearSelection();
                privateChatWindow(clientSocket, item, this);
            }
        });

        txtTitle.setText(txtTitle.getText() + " " + nickname);

        Thread listener = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String inputMessage = dis.readUTF();
                        System.out.println("PRIMLJENO U MAIN: " + inputMessage);
                        String[] data = inputMessage.split(":");
                        if (data[0].equals("message")) {
                            String from = data[1];
                            String message = data[2];
                            MessageBox(from, message);
                        } else if (data[0].equals("users")) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    listUsers.setItems(getUsers(data[1], nickname));
                                }
                            });
                        } else if (data[0].equals("private")) {
                            //onStatusChangeEvent(inputMessage);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        });
        listener.start();
    }

    @FXML
    void btnSendMessage(ActionEvent event) {
        sendMessage(txtMessage.getText());
    }

    private void sendMessage(String message) {
        String global = "message@all:";
        try {
            dos.writeUTF(global + message);
        } catch (IOException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
        MessageBox(nickname, message);
        txtMessage.clear();
    }

    private void sendPrivateMessage(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void MessageBox(String from, String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Text text = new Text(from + ": " + message);
                text.fontSmoothingTypeProperty().set(FontSmoothingType.GRAY);
                text.setFont(Font.font("Arial", 14));
                boxMessages.getChildren().add(text);
            }
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

    private void onStatusChangeEvent(Action status) {
        for (StatusChangeListener listener : listeners) {
            listener.onStatusChange(status);
        }
    }

    public void addListener(StatusChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

}
