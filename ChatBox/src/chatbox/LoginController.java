/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatbox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class LoginController implements Initializable {

    @FXML
    private BorderPane borderPane;

    @FXML
    private Label buttonExit;

    @FXML
    private Label buttonMinimize;

    @FXML
    private Button btnLogin;

    @FXML
    private ImageView textBubbleRight;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TextField txtNick;

    @FXML
    private Label loginError;

    @FXML
    private Label lblError;

    private double originX;
    private double originY;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        borderPane.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                originX = event.getSceneX();
                originY = event.getSceneY();
                borderPane.requestFocus();
            }
        });

        borderPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Stage primaryStage = (Stage) borderPane.getScene().getWindow();
                double xD = event.getScreenX() - originX;
                double yD = event.getScreenY() - originY;
                primaryStage.setX(xD);
                primaryStage.setY(yD);
            }
        });

        buttonExit.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Platform.exit();
            }
        });

        buttonMinimize.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Stage primaryStage = (Stage) borderPane.getScene().getWindow();

                //primaryStage.setIconified(true);
                primaryStage.initStyle(StageStyle.DECORATED);
            }
        });

        txtNick.setFocusTraversable(false);
        startAnimation(textBubbleRight, 2000);
    }

    private void startAnimation(Node node, double duration) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }

    @FXML
    void btnLogin(ActionEvent event) {

        try {
            Socket cs = new Socket("localhost", 1234);

            DataInputStream dis;
            DataOutputStream dos;

            if (cs.isConnected()) {
                dis = new DataInputStream(cs.getInputStream());
                dos = new DataOutputStream(cs.getOutputStream());

                dos.writeUTF("join:" + txtNick.getText());

                String response = dis.readUTF();
                System.out.println("SERVER: " + response);

                if (response.equals("server:nickname-error")) {
                    lblError.setText("Nickname already in use!");
                    startAnimation(loginError, 150);
                } else {
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                    a.setTitle("Connected");
                    a.showAndWait();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
