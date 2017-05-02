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
	List<Chat> chats = new ArrayList<Chat>();
	private List<ClientObserver> users = new ArrayList<>();
	

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
		URL whatismyip = new URL("http://checkip.amazonaws.com");
		BufferedReader in = new BufferedReader(new InputStreamReader(
		                whatismyip.openStream()));

		String ip_pub = in.readLine(); //you get the IP as a String
		System.out.println("Public IP is: " + ip_pub);
		String ip = InetAddress.getLocalHost().getHostAddress().toString();
		System.out.println("Local IP is: " + ip);
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
					System.out.println("Server read " + message);
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
	
	private void addChat(Chat chat){
		chats.add(chat);
	}
	
	private boolean userExists(String username){
		FindIterable<Document> iterable = chats_collection.find(eq("username", username));
	    return iterable.first() != null;
	}
	
	private boolean authUser(String username, String pass){
		FindIterable<Document> iterable = chats_collection.find(eq("username", username));
		FindIterable<Document> iterable2 = chats_collection.find(eq("pwd", pass));
		return (iterable.first() != null && iterable2.first() != null);
	}
	
	private void addUserToDB(String user, String pass){
		Boolean exists = false;
		for(ClientObserver u : users){
			if(u.getUser().equals(user))
				exists = true;
		}
		if(!exists){
			Document d = new Document();
			d.append("username", user);
			d.append("pwd", pass);
			List<String> arr = new ArrayList<String>();
			d.append("chats", arr);
			chats_collection.insertOne(d);
		}
	}
	
	private void addChatToDB(String chatname, ArrayList<String> users1){
		Boolean exists = false;
		for(Chat c : chats){
			if(c.getName().equals(chatname))
				exists = true;
		}
		if(!exists){
			Document d = new Document();
			d.append("chat_name", chatname);
			d.append("users", users1);
			List<Document> arr = new ArrayList<Document>();
			d.append("messages", arr);
			chats_collection.insertOne(d);
			for(String user : users1){
				addChatToUser(chatname, user);
			}
		}
	}
	
	private void addMsgToChat(String chatname, String user, String msg){
		chats_collection.updateOne(eq("chat_name", chatname), 
				new Document("$push", new Document("messages", new Document("user", user).append("msg", msg))));
	}
	
	private void addChatToUser(String chatname, String user){
		chats_collection.updateOne(eq("username", user), 
				new Document("$push", new Document("chats", chatname)));
	}
	private void parseCommand(String cmd, PrintWriter writer){
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
				ClientObserver p = new ClientObserver(writer, username);
				users.add(p);
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
				Document d = chats_collection.find(eq("username", username)).first();
				List<String> chats1 = (List<String>) d.get("chats");
				ClientObserver p = null; // Note that the user should already be in the users array
				for(int k = 0; k < users.size(); k++){
					if(users.get(k).getUser().equals(username)){
						p = users.get(k);
						break;
					}
				}
				p.setWriter(writer);
				if(chats1 != null){
					for(Chat c : chats){
						if(chats1.contains(c.getName())){
							c.addObserver(p);
						}
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
			Document d = chats_collection.find(eq("username", user)).first();
			writer.println("getchats");
			List<String> chats1 = (List<String>) d.get("chats");
			System.out.println("Chats are: " + d.get("chats").toString());
			if(chats1 != null){
				for(String c : chats1){
					writer.println(c);
				}
			}
			writer.println("END");
			writer.flush();
		}
		else if(tokens[0].equals("newchat")){
			Chat c = new Chat(tokens[1]);
			ArrayList<String> usrs = new ArrayList<>();
			for(int k = 2; k < tokens.length; k++){
				usrs.add(tokens[k]);
				for(ClientObserver p : users){
					if(p.getUser().equals(tokens[k])){
						c.addObserver(p);
						break;
					}
				}
			}
			c.notify("newchat\n" + tokens[1]);
			addChatToDB(c.getName(), usrs);
			addChat(c);
		}
		else if(tokens[0].equals("getchathistory")){
			String chat = tokens[1];
			writer.println("getchathistory");
			writer.println(tokens[1]);
			for(Chat c : chats){
				if(c.getName().equals(chat)){
					Document d = chats_collection.find(eq("chat_name", chat)).first();
					List<Document> messages = (List<Document>) d.get("messages");
					for(Document p : messages){
						writer.println(p.get("user") + ":\t" + p.get("msg"));
					}
					break;
				}
			}
			writer.println("END");
			writer.flush();
		}
		else if(tokens[0].equals("sendmsg")){
			for(Chat c : chats){
				if(c.getName().equals(tokens[1])){
					System.out.println("Chat found: " + c.getName());
					c.notify("sendmsg\n" + tokens[1] + "\n" + tokens[2] + "\n" + tokens[3]);
					addMsgToChat(tokens[1], tokens[2], tokens[3]);
					break;
				}
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
		MongoCursor<Document> cursor2 = chats_collection.find(exists("username")).iterator();

		try {
		    while (cursor.hasNext()) {
		    	Document x = cursor.next();
		    	Chat chat = new Chat(x.get("chat_name").toString());
		    	chats.add(chat);
		        System.out.println(x.toJson());
		    }
		    while(cursor2.hasNext()){
		    	Document x = cursor2.next();
		    	ClientObserver p = new ClientObserver(x.getString("username"));
		    	users.add(p);
		    	System.out.println(x.toJson());
		    }
		} finally {
		    cursor.close();
		}
	}

}
