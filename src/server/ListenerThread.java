package server;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;

import both.Message;

//responsible for receiving messages from the client
public class ListenerThread extends Thread {
	Server server = null;
	ClientConnection m_ClientConnection;

	// Construtor
	public ListenerThread(Server server, ClientConnection m_ClientConnection) {
		this.server = server;
		this.m_ClientConnection = m_ClientConnection;
	}

	// keep receving messages until the socket is closed
	public void run() {
		while (true) {
			byte[] incomingData = new byte[256];
			DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
			Message message = null;
			ByteArrayInputStream byte_stream =new ByteArrayInputStream(incomingPacket.getData());
			ObjectInputStream object_stream = null;
			try {
				object_stream = new ObjectInputStream(byte_stream);
				message = (Message)object_stream.readObject();
				System.out.println("message recevied" + message);
				// call method to process the message
				server.broadcast(message);
			} catch (Exception e) {
				e.printStackTrace();System.exit(-1);
			}
		}
	}

}
