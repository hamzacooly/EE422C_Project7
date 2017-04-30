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
	private List<ClientObserver> users;
	

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
			ClientHandler ch = new ClientHandler(socket);
			Thread t = new Thread(ch);
			t.start();
			System.out.println("Got a connection at: " + socket.getInetAddress().getHostAddress().toString());
		}
	}
	
	class ClientHandler implements Runnable{
		private BufferedReader reader;
		private PrintWriter writer;
		
		public ClientHandler(Socket clientSocket) {
			Socket sock = clientSocket;
			try {
				reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				writer = new PrintWriter(sock.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			System.out.println("running!");
			try {
				String message;
				while(!(message = reader.readLine()).equals("EXIT")){
					String command = "";
					command += message + "\n";
					while (!(message = reader.readLine()).equals("END")) {
						System.out.println("Server read " + message);
						command += message + "\n";
					}
					parseCommand(command, writer);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void updateChat(String msg, String chatID){
		Document d = (Document) chats_collection.find(eq("chat_name", chatID));
		
	}
	
	public void addChat(Chat chat){
		chats.add(chat);
	}
	
	public boolean userExists(String username){
		FindIterable<Document> iterable = chats_collection.find(eq("username", username));
	    return iterable.first() != null;
	}
	
	public boolean authUser(String username, String pass){
		FindIterable<Document> iterable = chats_collection.find(eq("username", username));
		FindIterable<Document> iterable2 = chats_collection.find(eq("pwd", pass));
		return (iterable.first() != null && iterable2.first() != null);
	}
	
	public void addUserToDB(String user, String pass){
		Document d = new Document();
		d.append("username", user);
		d.append("pwd", pass);
		List<Document> arr = new ArrayList<Document>();
		d.append("chats", arr);
		chats_collection.insertOne(d);
	}
	
	public void parseCommand(String cmd, PrintWriter writer){
		System.out.println("Got to parseCommand");
		System.out.println("Command is " + cmd);
		String[] tokens = cmd.split("\n");
		if(tokens[0].equals("newuser")){
			String username = tokens[1];
			String password = tokens[2];
			if(userExists(username)){
				System.out.println("Fail");
				writer.println("failure");
				writer.println("END");
				writer.flush();
			}
			else{
				System.out.println("Successful!");
				addUserToDB(username, password);
				writer.println("success");
				writer.println("END");
				writer.flush();
			}
		}
		else if(tokens[0].equals("login")){
			String username = tokens[1];
			String password = tokens[2];
			if(authUser(username, password)){
				System.out.println("Success LOGIN");
				FindIterable<Document> d = chats_collection.find(eq("username", username));
				Document doc = d.first();
				List<String> chats1 = (List<String>) doc.get("chats");
				for(Chat c : chats){
					if(chats.contains(c.getName())){
						ClientObserver p = new ClientObserver(writer, username);
						c.addObserver(p);
						users.add(p);
					}
				}
				writer.println("success");
				writer.println("END");
				writer.flush();
			}
			else{
				System.out.println("FAIL LOGIN!");
				writer.println("failure");
				writer.println("END");
				writer.flush();
			}
		}
		else if(tokens[0].equals("getchats")){
			String user = tokens[1];
			FindIterable<Document> d = chats_collection.find(eq("username", user));
			Document doc = d.first();
			writer.println("printchats");
			List<String> chats1 = (List<String>) doc.get("chats");
			for(String c : chats1){
				writer.println(c);
			}
			writer.println("END");
			writer.flush();
		}
		else if(tokens[0].equals("newchat")){
			Chat c = new Chat(tokens[1]);
			for(int k = 2; k < tokens.length; k++){
				for(ClientObserver p : users){
					if(p.getUser().equals(tokens[k])){
						c.addObserver(p);
						break;
					}
				}
			}
			chats.add(c);
		}
		else if(tokens[0].equals("getchathistory")){
			String chat = tokens[1];
			writer.println("msg");
			for(Chat c : chats){
				if(c.getName().equals(chat)){
					FindIterable<Document> d = chats_collection.find(eq("chat_name", chat));
					Document doc = d.first();
					Map<String, String> msgs = (Map<String, String>) doc.get("messages");
					for(String k : msgs.keySet()){
						writer.println("k:" + msgs.get(k));
					}
					break;
				}
			}
			writer.flush();
		}
		else if(tokens[0].equals("sendmsg")){
			for(Chat c : chats){
				if(c.getName().equals(tokens[1])){
					c.notify(tokens[2] + tokens[3]);
				}
				break;
			}
		}
	}
	
	private void setupDB(String ip) throws IOException{
		Process p = Runtime.getRuntime().exec("cmd /c cd \"C:\\Program Files\\MongoDB\\Server\\3.4\\bin\" & mongod --dbpath \"C:\\data\\Proj7\" --port 2017");
		mongoClient = new MongoClient(ip, 2017);
		db = mongoClient.getDatabase("Proj7");
	}
	
	private void getDBHistory(){
		chats_collection = db.getCollection("Chats");
		System.out.println("There are " + chats_collection.count() + " documents");
		MongoCursor<Document> cursor = chats_collection.find(exists("chat_name")).iterator();
		try {
		    while (cursor.hasNext()) {
		    	Document x = cursor.next();
		    	Chat chat = new Chat(x.get("chat_name").toString());
		    	chats.add(chat);
		        System.out.println(x.toJson());
		    }
		} finally {
		    cursor.close();
		}
	}

}
