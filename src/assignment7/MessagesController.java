package assignment7;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
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
public class MessagesController implements Initializable {

    private BufferedReader reader;
    private PrintWriter writer;
    private String chatname;
    public static ObservableList<String> messages = FXCollections.observableArrayList();

    @FXML
    private Button back_butt;
    @FXML
    private TextArea name_text;
    @FXML
    private TextField msg_field;
    @FXML
    private Button send_butt;
    @FXML
    private ListView<String> message_list;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	
    	message_list.setItems(messages);
    	
    	messages.addListener(new ListChangeListener<String>(){

			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c) {
				// TODO Auto-generated method stub
				message_list.setItems(messages);
			}
    		
    	});

        //name_text.setBackground(Background.EMPTY);

        msg_field.setOnKeyPressed((event) -> {
            if (event.getCode().equals(KeyCode.ENTER)){
                //get text
                //send to server via stream
                String msg = msg_field.getText();
                String name = name_text.getText();
                //message: [chatname], [sender], [message]
                writer.println("sendmsg");
                writer.println(name);
                writer.println(ClientMain.user);
                writer.println(msg);
                writer.println("END");
                writer.flush();
            }
        });

        send_butt.setOnAction((event) -> {
            //get text
            //send to server via stream
            String msg = msg_field.getText();
            String name = name_text.getText();
            //message: [chatname], [sender], [message]
            writer.println("sendmsg");
            writer.println(name);
            writer.println(ClientMain.user);
            writer.println(msg);
            writer.println("END");
            writer.flush();
        });

        back_butt.setOnAction((event) -> {
            //go back to main scene
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
            writer.println("getchats");
			writer.println(ClientMain.user);
			writer.println("END");
			writer.flush();
        });

    }

    public void setStreams (BufferedReader br, PrintWriter pw){
        reader = br;
        writer = pw;
    }
    
    public void setChatname(String c){
    	chatname = c;
    	name_text.setText(c);
    }
}
