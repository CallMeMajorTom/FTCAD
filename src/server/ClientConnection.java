package server;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import both.Message;

/**
 * 
 * @author brom
 */
// Mainly responsible for sending messages
public class ClientConnection {

	Socket socket;
	ObjectInputStream r;
	ObjectOutputStream o;

	// Constructor
	public ClientConnection(Socket socket) {
		this.socket = socket;
		try {
			o = new ObjectOutputStream(socket.getOutputStream());
			r = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// send message to client
	public void sendMessage(Message message) {
		System.out.println("reply sent");
		try {
			o.writeObject(message);
			o.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public ObjectInputStream getObjectInputStream() {
		return r;
	}
}
