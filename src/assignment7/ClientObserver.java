package assignment7;

import java.io.PrintWriter;
import java.util.Observable;
import java.util.Observer;

public class ClientObserver implements Observer {
	private PrintWriter writer;
	private String user;
	
	public ClientObserver(PrintWriter writer, String u){
		this.writer = writer;
		user = u;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		writer.println(arg1);
		writer.flush();
	}

	public String getUser() {
		return user;
	}

}
