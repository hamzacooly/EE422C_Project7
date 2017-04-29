package assignment7;

import java.io.*;
import java.net.*;
import java.util.Observable;
import java.util.Observer;

public class ClientMain implements Observer {

	@Override
	public void update(Observable server, Object obj) {
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
		Socket socket = new Socket("10.146.21.103", 2017);
		PrintWriter writer = new PrintWriter(socket.getOutputStream());
		writer.println("swag");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
