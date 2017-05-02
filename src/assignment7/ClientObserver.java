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
	
	public ClientObserver(String u){
		user = u;
		writer = null;
	}
	
	public void setWriter(PrintWriter writer) {
		this.writer = writer;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if(writer == null)
			return;
		System.out.println("Sending msg " + arg1.toString());
		writer.println(arg1);
		writer.println("END");
		writer.flush();
	}

	public String getUser() {
		return user;
	}

}
