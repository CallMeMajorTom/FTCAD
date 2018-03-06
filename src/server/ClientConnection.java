package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import both.Message;
import both.Worker;

// responsible for sending messages, indirectly receiving messages, ordering, diffusion, 
// acknowledge received messages
public class ClientConnection extends Thread {

	private DatagramSocket m_socket = null;
	private InetAddress m_ClientAddress = null;
	private final int mPort;
	private final String mAddress;
	private ArrayList<Message> mSentMessages = new ArrayList<Message>();
	private ArrayList<Message> mReceiveMessages = new ArrayList<Message>();

	// Constructor
	public ClientConnection(String clientName, int ClientPort, DatagramSocket fesocket) {
		this.mAddress = clientName;
		this.mPort = ClientPort;
		this.m_socket = fesocket;
	}

	public void setmSentMessages(ArrayList<Message> mSentMessages) {
		this.mSentMessages = mSentMessages;
	}

	public void setmReceiveMessages(ArrayList<Message> mReceiveMessages) {
		this.mReceiveMessages = mReceiveMessages;
	}

	// send message to client
	public void sendMessage(Message message) throws IOException {
		System.out.println("reply sent");
		mSentMessages.add(message);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream object_output = new ObjectOutputStream(outputStream);
		object_output.writeObject(message);
		byte[] data = outputStream.toByteArray();
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, m_ClientAddress, mPort);
		new Thread(new Worker(sendPacket, m_socket, message)).start();

	}
	
	public void receiveMessage(){
		
	}

	public void run() {
		
	}

}
