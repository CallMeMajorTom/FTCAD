package client;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import both.Message;

/**
 *
 * @author brom
 */
public class ServerConnection {
	String hostName;
	int port;
	Socket socket;
	ObjectOutputStream o;
	ObjectInputStream i;

	// Construtor
	public ServerConnection(String hostName, int port) {
		this.hostName = hostName;
		this.port = port;
	}

	// Connect to server
	public boolean connect() {
		try {
			socket = new Socket(hostName, port);
			o = new ObjectOutputStream(socket.getOutputStream());
			i = new ObjectInputStream(socket.getInputStream());
			// Send client name to join

		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	// receive message from server
	public Message receiveMessage() {
		Message message = null;
		try {
			message = (Message) i.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return message;
	}

	// send message to server
	public void sendMessage(Message message) {
		try {
			o.writeObject(message);
			o.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("message sent");
	}

}
