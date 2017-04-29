package assignment7;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

public class ClientMain extends Application implements Observer {
	private BufferedReader reader;
	private PrintWriter writer;

	@Override
	public void update(Observable server, Object obj) {
		// TODO Auto-generated method stub
		
	}

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

		//thread start
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			setUpNetworking();
		} catch (Exception e) {
			e.printStackTrace();
		}
		FXMLLoader loader = new FXMLLoader(getClass().getResource("chatUILogin.fxml"));
		Parent root = loader.load();
		LoginController L = loader.getController();
		L.setStreams(this.reader, this.writer);
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Chat Client");
		primaryStage.show();
	}

}
