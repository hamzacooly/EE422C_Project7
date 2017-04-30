package assignment7;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class ClientMain extends Application implements Observer {
	private static BufferedReader reader;
	private static PrintWriter writer;
	public static String user;
	private String incoming;
	private Stage stage;
	private ArrayList<Chat> chat_list;

	@Override
	public void update(Observable chat, Object obj) {
		// TODO Auto-generated method stub


		
	}

	Runnable updateScene1 = new Runnable() {

		@Override
		public void run() {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("chatUIMain.fxml"));
			Parent root = null;
			try {
				root = fxmlLoader.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
			ClientController C = fxmlLoader.getController();
			C.setStreams(ClientMain.reader, ClientMain.writer);
			Scene scene = new Scene(root);
			stage.setScene(scene);
			writer.println("getchats");
			writer.println(user);
			writer.println("END");
			writer.flush();
		}
	};

	Runnable updateScene2 = new Runnable() {
		@Override
		public void run() {
			//Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
			FXMLLoader loader2 = new FXMLLoader(getClass().getResource("chatUIMessages.fxml"));
			Parent root2 = null;
			try {
				root2 = loader2.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
			MessagesController M = loader2.getController();
			M.setStreams(ClientMain.reader, ClientMain.writer);
			//M.chatname = ((Text) o).getText();
			Scene scene2 = new Scene(root2);
			stage.setScene(scene2);
			writer.println("getchathistory");
			writer.println(M.chatname);
			writer.println("END");
			writer.flush();
		}
	};

	public static void main (String[] args) {
		// TODO Auto-generated method stub
		launch(args);

	}

	private void setUpNetworking() throws Exception {
		Socket socket = new Socket("10.147.119.215", 12017); //IP, Port
		InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
		reader = new BufferedReader(streamReader);
		writer = new PrintWriter(socket.getOutputStream());
		//new thread
		(new Thread (() -> {
			String message;
			try {
				while(true) {
					incoming = "";
					while (!(message = reader.readLine()).equals("END")) {
						incoming += message + "\n";
					}
					parseMessage(incoming);
				}
			} catch(IOException e){
				e.printStackTrace();
			}
		})).start();
		//thread start
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			setUpNetworking();
		} catch (Exception e) {
			e.printStackTrace();
		}
		stage = primaryStage;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("chatUILogin.fxml"));
		Parent root = loader.load();
		LoginController L = loader.getController();
		L.setStreams(this.reader, this.writer);
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Chat Client");
		primaryStage.show();

	}

	public String getIncoming(){
		return incoming;
	}

	public void parseMessage(String msg){
		String[] tokens = msg.split("\n");
		if (tokens[0].equals("success")){
			//Stage stage = (Stage)label.getScene().getWindow();
			Platform.runLater(updateScene1);
		}
		else if (tokens[0].equals("failure")){
			//Text t = L.getInvalidText().setText("Invalid login. Please try again.");
		}
		else if (tokens[0].equals("msg")){
			int i = 1;
			while (!tokens[i].equals(user)) {
				MessagesController.message_list.getChildrenUnmodifiable().add(new Text(tokens[i]));
				i++;
			}
		}
		else if (tokens[0].equals("chats")){
			int i = 1;
			while (!tokens[i].equals(user)) {
				Text t = new Text(tokens[i]);
				t.setOnMouseClicked((event) -> {
					Platform.runLater(updateScene2);
				});
				ClientController.chat_list.getChildrenUnmodifiable().add(t);
				chat_list.add(new Chat(tokens[i]));
				i++;
			}
		}
		else if (tokens[0].equals("newmsg")){
			MessagesController.message_list.getChildrenUnmodifiable().add(new Text(tokens[2]));
		}

		else if (tokens[0].equals("newchat")){
			MessagesController.name_text.setText(tokens[1]);
			Text newT = new Text(tokens[1]);
			newT.setOnMouseClicked((event) -> {
				Platform.runLater(updateScene2);
			});
			ClientController.chat_list.getChildrenUnmodifiable().add(newT);
		}
	}
}
