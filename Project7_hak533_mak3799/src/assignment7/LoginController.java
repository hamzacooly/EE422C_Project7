package assignment7;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Ali Kedwaii on 4/29/2017.
 */
public class LoginController implements Initializable {

    private BufferedReader reader;
    private PrintWriter writer;

    @FXML
    private TextField username_text;
    @FXML
    private TextField password_text;
    @FXML
    private Button create_user_butt;
    @FXML
    private Button login_butt;
    @FXML
    private Text invalid_text;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        create_user_butt.setOnAction((event) -> {
            //sends user/pass to server
            String userString = username_text.getText();
            String passString = password_text.getText();
            //db makes new user
            writer.println("newuser");
            writer.println(userString);
            writer.println(passString);
            writer.println("END");
            writer.flush();
            try {
                Thread.sleep(5);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            //validateLogin(event);
        });

        login_butt.setOnAction((event) -> {
            //sends stuff to server
            String userString = username_text.getText();
            String passString = password_text.getText();
            writer.println("login");
            writer.println(userString);
            writer.println(passString);
            writer.println("END");
            writer.flush();
            ClientMain.user = userString;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            //validateLogin(event);
        });

    }

    public void setStreams (BufferedReader br, PrintWriter pw){
        reader = br;
        writer = pw;
    }

    public void validateLogin (ActionEvent event) {
        //if invalid: print invalid text
        //else go to main screen
        System.out.println("Ready to read.");

            String line;
            String response = "";
            try {
                while (!(line = reader.readLine()).equals("END")) {
                    response += line;
                }
            } catch(IOException e){
                e.printStackTrace();
            }
            loginResponse(response, event);



    }
    public void loginResponse(String response, ActionEvent event){
        String[] tokens = response.split("\n");
        if (tokens[0].equals("success")){
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("chatUIMain.fxml"));
            Parent root = null;
            try {
                root = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ClientController C = fxmlLoader.getController();
            C.setStreams(this.reader, this.writer);
            Scene scene = new Scene(root);
            stage.setScene(scene);
        }
        else if (tokens[0].equals("failure")){
            invalid_text.setText("Invalid login. Please try again.");
        }
    }
    public Text getInvalidText(){
        return invalid_text;
    }
}

