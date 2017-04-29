package assignment7;

import java.util.*;

public class Chat extends Observable {
	private String chatname;
	
	public Chat(String s){
		chatname = s;
	}
	
	public void notify(String msg){
		System.out.println("server read " + msg);
		setChanged();
		notifyObservers(msg);
	}
	
	public String getName(){
		return chatname;
	}
}
