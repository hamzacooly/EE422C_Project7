package assignment7;


import java.util.*;

import org.bson.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.io.*;
import com.mongodb.*;
import com.mongodb.client.*;
import static com.mongodb.client.model.Filters.*;

public class ServerMain{
	
	MongoClient mongoClient;
	MongoDatabase db;
	MongoCollection<Document> chats_collection;
	ServerSocket server;
	Socket socket;	
	List<Chat> chats;
	

	public static void main(String[] args) {				
		// Run the server
		try{
			new ServerMain().run();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void run() throws Exception{
		chats = new ArrayList<Chat>();
		String ip = InetAddress.getLocalHost().getHostAddress().toString();
		System.out.println("Server IP is: " + ip);
		server = new ServerSocket(12017);
		setupDB(ip);
		getDBHistory();
		while (true) {
			socket = server.accept();
			
			System.out.println("Got a connection at: " + socket.getInetAddress().getHostAddress().toString());
		}
	}
	
	public void updateChat(String msg, String chatID){
		// TODO Auto-generated method stub
	}
	
	public void addChat(Chat chat){
		chats.add(chat);
	}
	
	public boolean userExists(String username){
		if(((Document) chats_collection.find(eq("user", username)).limit(1)).size() == 0)
			return false;
		else
			return true;
	}
	
	public void parseCommand(String cmd){
		
	}
	
	private void setupDB(String ip) throws IOException{
		Process p = Runtime.getRuntime().exec("cmd /c cd \"C:\\Program Files\\MongoDB\\Server\\3.4\\bin\" & mongod --dbpath \"C:\\data\\Proj7\" --port 2017");
		mongoClient = new MongoClient(ip, 2017);
		db = mongoClient.getDatabase("Proj7");
	}
	
	private void getDBHistory(){
		chats_collection = db.getCollection("Chats");
		MongoCursor<Document> cursor = chats_collection.find().iterator();
		try {
		    while (cursor.hasNext()) {
		        System.out.println(cursor.next().toJson());
		    }
		} finally {
		    cursor.close();
		}
	}
	
//	class ClientHandler implements Runnable{
//		private BufferedReader reader;
//		
//
//		public ClientHandler(Socket clientSocket) {
//			Socket sock = clientSocket;
//			try {
//				reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//
//		public void run() {
//			String message;
//			try {
//				while ((message = reader.readLine()) != null) {
//					System.out.println("server read "+message);
//					setChanged();
//					notifyObservers(message);
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}

}
