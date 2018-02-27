package client;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

import both.Message;

/**
 *
 * @author brom
 */
public class FEConnection {
	
    private DatagramSocket m_socket = null;
    private InetAddress m_FEAddress = null;
    private int m_FEPort = -1;
    
    public FEConnection(String hostName, int port) throws SocketException, UnknownHostException {
    	m_FEPort = port;
		m_FEAddress = InetAddress.getByName(hostName);
		m_socket = new DatagramSocket();
    }

    
    public String receiveChatMessage() throws IOException {
    	byte[] incomingData = new byte[256];
    	DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
    	m_socket.send(incomingPacket);
    	String response = new String(incomingPacket.getData());
    	
	    byte[] message = new byte[256];
	    DatagramPacket packet = new DatagramPacket(message, message.length);
	     m_socket.receive(packet);
	    String received = new String(packet.getData(),0,packet.getLength());
	    return received;
    }
    
    public void sendChatMessage(Message message) throws IOException {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	ObjectOutputStream object_output = new ObjectOutputStream(outputStream);
    	object_output.writeObject(message);
    	byte[] data = outputStream.toByteArray();
    	DatagramPacket sendPacket = new DatagramPacket(data, data.length, m_FEAddress,m_FEPort);
    	m_socket.send(sendPacket);
    }
    
	/*String hostName;
	int port;
	Socket socket;
	ObjectOutputStream o;
	ObjectInputStream i;

	// Construtor
	public FEConnection(String hostName, int port) {
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
	}*/

}
