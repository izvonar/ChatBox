/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatbox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Ivan
 */
public class PrivateWindowController implements Initializable {

    @FXML
    private BorderPane borderPane;

    @FXML
    private TextField txtMessage;

    @FXML
    private VBox boxMessages;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Label buttonExit;

    @FXML
    private Button btnSend;

    @FXML
    private Text txtTitle;

    private double originX;
    private double originY;
    String nickname;
    String target;
    private List<StatusChangeListener> listeners = new ArrayList();

    public PrivateWindowController(MainWindowController ctrl, String nickname, String target) throws IOException {
        this.nickname = nickname;
        this.target = target;
        ctrl.addListener(new StatusChangeListener() {
            @Override
            public void onStatusChange(String inputMessage) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("U KONTROLERU PRIVATE: " + inputMessage);
                        String[] data = inputMessage.split(":");
                        String from = data[1];
                        String message = data[2];
                        MessageBox(from, message);
                    }
                });
            }
        });
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
            txtMessage.getScene().getWindow().hide();
        });

        anchorPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    sendMessage(txtMessage.getText());
                }
            }
        });
        txtTitle.setText(txtTitle.getText() + " @" + target);
    }

    @FXML
    void btnSendMessage(ActionEvent event) {
        sendMessage(txtMessage.getText());
    }

    private void sendMessage(String message) {
        String privateMsg = "private:" + target + ":";
        MessageBox(nickname, message);
        onStatusChangeEvent(privateMsg + message);
        txtMessage.clear();
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

}
