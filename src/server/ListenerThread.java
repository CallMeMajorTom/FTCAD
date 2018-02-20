package server;

import java.io.IOException;
import java.io.ObjectInputStream;

import both.Message;

//responsible for receiving messages from the client
public class ListenerThread extends Thread {
	Server server = null;
	ClientConnection cc;
	ObjectInputStream r;

	// Construtor
	public ListenerThread(Server server, ClientConnection cc) {
		this.server = server;
		this.cc = cc;
		r = cc.getObjectInputStream();
	}

	// keep receving messages until the socket is closed
	public void run() {
		while (true) {
			Message message = null;
			try {
				message = (Message) r.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("message recevied" + message);
			// call method to process the message
			server.broadcast(message);
		}
	}

}
