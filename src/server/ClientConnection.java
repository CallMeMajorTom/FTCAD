package server;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

import both.Message;

/**
 * 
 * @author brom
 */
// Mainly responsible for sending messages
public class ClientConnection {

	private DatagramSocket m_socket = null;
	private InetAddress m_ClientAddress = null;
	private int m_ClientPort = -1;

	// Constructor
	public ClientConnection(String hostName, int ClientPort) throws SocketException, UnknownHostException {
		this.m_ClientAddress = InetAddress.getByName(hostName);
		this.m_ClientPort = ClientPort;
		this.m_socket = new DatagramSocket();
	}

	// send message to client
	public void sendMessage(Message message) throws IOException {
		System.out.println("reply sent");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream object_output = new ObjectOutputStream(outputStream);
		object_output.writeObject(message);
		byte[] data = outputStream.toByteArray();
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, m_ClientAddress,m_ClientPort);
		m_socket.send(sendPacket);
	}
}
