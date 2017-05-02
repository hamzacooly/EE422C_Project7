package assignment7;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Ali Kedwaii on 4/26/2017.
 */
public class ClientController implements Initializable {

    private BufferedReader reader;
    private PrintWriter writer;
    public static ObservableList<Text> chatnames;


    @FXML
    private TextField search_text;
    @FXML
    private Button search_butt;
    @FXML
    private Button new_chat_butt;
    @FXML
    private Button settings_butt;
    @FXML
    private ScrollPane stories_pane;
    @FXML
    private HBox stories_box;
    @FXML
    private Button story_add_butt;
    @FXML
    public static ListView chat_list;
    @FXML
    private Button messages_tab;
    @FXML
    private Button groups_tab;
    @FXML
    private Button friends_tab;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        new_chat_butt.setOnAction((event) -> {
            //dialog box
            //ask for user(s) to chat
            TextInputDialog d = new TextInputDialog();
            d.setContentText("Enter chat and user names: ");
            d.showAndWait().ifPresent(val -> {
                String[] ppl = d.getEditor().getText().split(" ");
                writer.println("newchat");
                for (String s: ppl){
                    writer.println(s);
                }
                writer.println("END");
                writer.flush();
                Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                FXMLLoader loader2 = new FXMLLoader(getClass().getResource("chatUIMessages.fxml"));
                Parent root2 = null;
                try {
                    root2 = loader2.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MessagesController M = loader2.getController();
                M.setStreams(this.reader, this.writer);
                M.chatname = ppl[0];
                Scene scene2 = new Scene(root2);
                stage.setScene(scene2);
            });
        });

        search_text.setOnKeyPressed((event) -> {
            if (event.getCode().equals(KeyCode.ENTER)){
                //search
                //send query
            }
        });

        search_butt.setOnAction((event) -> {
            //search
        });


//        chat_list.setOnMouseClicked((event) -> {
//            Object o = chat_list.getSelectionModel().getSelectedItem();
//            //get data from item
//            //new scene
//            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
//            FXMLLoader loader2 = new FXMLLoader(getClass().getResource("chatUIMessages.fxml"));
//            Parent root2 = null;
//            try {
//                root2 = loader2.load();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            MessagesController M = loader2.getController();
//            M.setStreams(this.reader, this.writer);
//            M.chatname = ((String) o);
//            Scene scene2 = new Scene(root2);
//            stage.setScene(scene2);
//            writer.println("getchathistory");
//            writer.println(M.chatname);
//            writer.println("END");
//            writer.flush();
//        });

        messages_tab.setOnAction((event) -> {
            //swap list
        });

        groups_tab.setOnAction((event) -> {
            //
        });

    }

    public void setStreams (BufferedReader br, PrintWriter pw){
        reader = br;
        writer = pw;
    }
}
