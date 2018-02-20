package server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import both.Message;

public class Server {
	// an array list of Clients
	private ArrayList<ClientConnection> connectedClients = new ArrayList<ClientConnection>();

	// adds a client, returns false if name is already used
	public synchronized boolean addClient(Socket socket) {
		ClientConnection c;
		ClientConnection cc = new ClientConnection(socket);
		connectedClients.add(cc);
		ListenerThread t = new ListenerThread(this, cc);
		t.start();
		System.out.println("Client added");
		return true;
	}

	public synchronized void broadcast(Message message) {
		for (ClientConnection cc : connectedClients) {
			cc.sendMessage(message);
		}
	}

}
