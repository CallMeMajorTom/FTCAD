package server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import both.Message;

import javax.print.attribute.standard.MediaSize;

public class Server {
	// an array list of Clients
	private ArrayList<ClientConnection> connectedClients = new ArrayList<ClientConnection>();
	private ArrayList Other_Server_Port = new ArrayList();
	private DatagramSocket m_socket;
	private DatagramPacket toSend;
	private final int FEPort;
	private final int Port;

	public static void main(String[] args) throws SocketException {
		if (args.length < 1) {
			System.err.println("Usage: java Server portnumber");
			System.exit(-1);
		}
		int Port = Integer.parseInt(args[0]);
		int FEPort = Integer.parseInt(args[1]);
		ArrayList Server_Port = new ArrayList();
		for(int i = 2; i < args.length; i++)
			Server_Port.add(Integer.parseInt(args[i]));

		Server instance = new Server(Port,FEPort,Server_Port);
	}

	public Server(int m_Port, int m_FEPort, ArrayList Server_Port) throws SocketException {
			Port = m_Port;
			m_socket = new DatagramSocket(m_Port);
			FEPort = m_FEPort;
			Other_Server_Port = Server_Port;
	}


	private void listenForClientMessages() throws IOException, ClassNotFoundException {

		System.out.println("Waiting for client messages... ");

		do {
			byte[] incomingData = new byte[256];
			DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
			ByteArrayInputStream byte_stream =new ByteArrayInputStream(incomingPacket.getData());
			ObjectInputStream object_stream = new ObjectInputStream(byte_stream);
			Message message = (Message)object_stream.readObject();
		}
		while(true);
	}

	public synchronized boolean addClient(String hostName, int port) throws SocketException, UnknownHostException {
		ClientConnection m_ClientConnection = new ClientConnection(hostName,port);
		connectedClients.add(m_ClientConnection);
		ListenerThread receive_message = new ListenerThread(this, m_ClientConnection);
		receive_message.start();
		System.out.println("Client added");
		return true;
	}

	public synchronized void broadcast(Message message) throws IOException {
		for (ClientConnection cc : connectedClients) {
			cc.sendMessage(message);
		}
	}

}
