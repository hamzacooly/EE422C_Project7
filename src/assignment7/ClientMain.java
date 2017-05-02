package assignment7;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.fxml.FXMLLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;


public class ClientMain extends Application {
	private BufferedReader reader;
	private PrintWriter writer;
	public static String user;
	private String incoming;
	private Stage stage;


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
			C.setStreams(reader, writer);
			Scene scene = new Scene(root);
			stage.setScene(scene);
			writer.println("getchats");
			writer.println(user);
			writer.println("END");
			writer.flush();
		}
	};

	class updateScene2 implements Runnable {
		String name;

		public updateScene2(String s){
			name = s;
		}


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
			M.setStreams(reader, writer);
			M.setChatname(name);
			Scene scene2 = new Scene(root2);
			stage.setScene(scene2);
			writer.println("getchathistory");
			writer.println(name);
			writer.println("END");
			writer.flush();
		}
	};

	public static void main (String[] args) {
		// TODO Auto-generated method stub
		launch(args);

	}

	private void setUpNetworking() throws Exception {
		Socket socket = new Socket("10.146.225.1", 12017); //IP, Port
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
		L.setStreams(reader, writer);
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
		else if (tokens[0].equals("getchathistory")){
			int i = 2;
			MessagesController.messages.clear();
			while (i < tokens.length) {
				if (!MessagesController.messages.contains(tokens[i])){
					ArrayList<String> m = new ArrayList<>();
					m.add(tokens[1]);
					m.add(tokens[i]);
					MessagesController.messages.add(m);
				}
				i++;
			}
		}
		else if (tokens[0].equals("getchats")){
			int i = 1;
			while (i < tokens.length) {
				Text t = new Text(tokens[i]);
				Boolean exists = false;
				for(Text txt : ClientController.chatnames){
					if(txt.getText().equals(tokens[i]))
						exists = true;
				}
				if(!exists)
					ClientController.chatnames.add(t);
				i++;
			}
		}
		else if (tokens[0].equals("sendmsg")){
			ArrayList<String> m = new ArrayList<>();
			m.add(tokens[1]);
			String mes = tokens[2] + ":\t" + tokens[3];
			m.add(mes);
			if(!tokens[2].equals(user)){
				String musicFile = "communication-channel.mp3";
	        	Media sound = new Media(new File(musicFile).toURI().toString());
	        	MediaPlayer mediaPlayer = new MediaPlayer(sound);
	        	mediaPlayer.play();
			}
			Platform.runLater(new Runnable() {
			    public void run() {
			    	MessagesController.messages.add(m);
			    }
			});
		}

		else if (tokens[0].equals("newchat")){
			Text t = new Text(tokens[1]);
			Platform.runLater(new Runnable() {
			    public void run() {
			    	ClientController.chatnames.add(t);
			    }
			});
			
		}
	}
}
