package chatbox;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import model.Action;
import model.Data;
import model.ServerAction;
import model.User;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

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

        buttonMinimize.setOnMousePressed((MouseEvent event) -> {
            Stage primaryStage = (Stage) borderPane.getScene().getWindow();
            //primaryStage.setIconified(true);
            primaryStage.initStyle(StageStyle.DECORATED);
        });

        borderPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                startConnection();
            }
        });

        //startMainWindow(new Socket(),new User("test"));

        txtNick.setFocusTraversable(false);
        startAnimation(textBubbleRight, 2000);
    }

    @FXML
    void btnLogin(ActionEvent event) {
        startConnection();
    }

    private void startAnimation(Node node, double duration) {
        loginError.setVisible(true);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }

    private void updateStatusLabel(String message) {
        Platform.runLater(() -> lblError.setText(message));
    }

    private void messageBox(String title, String message) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle(title);
            a.setContentText(message);
            a.showAndWait();
        });
    }

    private void startConnection() {
        updateStatusLabel("");
        loginError.setVisible(false);
        if (txtNick.getText().equals("")) {
            updateStatusLabel("Please enter your nickname!");
            return;
        }
        ConnectToServer connection = new ConnectToServer(txtNick.getText());
        connection.addListener(status -> {
            ServerAction serverResponse = Data.readServerAction(status);
            if (serverResponse.getAction() == Action.UserConnected) {
                startMainWindow(connection.getClientSocket(), serverResponse.getUser());
                updateStatusLabel("Connected!");
            } else if (serverResponse.getAction() == Action.NicknameTaken) {
                updateStatusLabel("Nickname already in use!");
                startAnimation(loginError, 150);
            } else{
                updateStatusLabel("ChatBox server is offline!");
            }
        });
        connection.start();
    }

    private void startMainWindow(Socket socket, User user) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mainWindow.fxml"), ResourceBundle.getBundle("resources/fontawesome"));
                Stage stage = new Stage();
                try {
                    MainWindowController ctrl = new MainWindowController(socket, user);
                    loader.setController(ctrl);
                    Scene scene = new Scene(loader.load());
                    stage.initStyle(StageStyle.UNDECORATED);
                    stage.setTitle("ChatBox");
                    stage.setScene(scene);
                    stage.show();
                    txtNick.getScene().getWindow().hide();
                } catch (IOException ex) {
                    Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
}
