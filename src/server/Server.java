package server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import both.Message;

public class Server {
	private ArrayList<ClientConnection> mClientConnections = new ArrayList<ClientConnection>();//array list of Clients
	private ArrayList<ReplicaConnection> mReplicaConnections = new ArrayList<ReplicaConnection>();
	private ServerSocket mTSocket;
	private DatagramSocket mUSocket;
	private DatagramPacket mToSend;
	private final int mFEPort;
	private final int mPort;
	private boolean primary;

	public static void main(String[] args) throws SocketException {
		if (args.length < 2) {
			System.err.println("Need both port and feport");
			System.exit(-1);
		}
		int Port = Integer.parseInt(args[0]);
		int FEPort = Integer.parseInt(args[1]);
		Server instance = new Server(Port, FEPort);
		for(int i = 2; i < args.length; i++)
			instance.addReplicas(Integer.parseInt(args[i]));
		instance.init();
	}

	public Server(int Port, int FEPort) throws SocketException {
			mPort = Port;
			mFEPort = FEPort;
			mUSocket = new DatagramSocket(Port);
			try {mTSocket = new ServerSocket(mPort);} catch (IOException e) {e.printStackTrace(); System.exit(-1);}
			primary = false;
			//FEConnection = new FEConnection("localhost", mFEPort);
	}

	private void addReplicas(int port) {
		ReplicaConnection rep = new ReplicaConnection(port, this, mTSocket);
		Thread thread = new Thread(rep);
		thread.start();
		mReplicaConnections.add(rep);
	}

	private void init() {
		do {
			if(-1 == whoIsPrimary()) election(); else join();
			if(primary)actAsPrimary(); else actAsBackup();
		} while(true);
	}

	private void actAsBackup() {
	}

	private void actAsPrimary() {
	}

	private void join() {
	}

	private void election() {
	}

	private int whoIsPrimary() {
		int winner = -1;
		ReplicaConnection c;
		for(Iterator<ReplicaConnection> i = mReplicaConnections.iterator(); i.hasNext();) {
			c=i.next();
			c.sendMessage("whoIsPrimary");
			int r = c.receiveMessage2();
			if(-1!=r) {winner = r;	break;}
		}
		return winner;
	}

	private void listenForClientMessages() {
		System.out.println("Waiting for client messages... ");
		do {
			byte[] incomingData = new byte[256];
			DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
			ByteArrayInputStream byte_stream =new ByteArrayInputStream(incomingPacket.getData());
			try {
				ObjectInputStream object_stream = new ObjectInputStream(byte_stream);
				Message message = (Message)object_stream.readObject();
			} catch (Exception e) {e.printStackTrace(); System.exit(-1);}
		}
		while(true);
	}

	public synchronized boolean addClient(String hostName, int port) throws SocketException, UnknownHostException {
		ClientConnection m_ClientConnection = new ClientConnection(hostName,port);
		mClientConnections.add(m_ClientConnection);
		ListenerThread receive_message = new ListenerThread(this, m_ClientConnection);
		receive_message.start();
		System.out.println("Client added");
		return true;
	}

	public synchronized void broadcast(Message message) throws IOException {
		for (ClientConnection cc : mClientConnections) {
			cc.sendMessage(message);
		}
	}

	synchronized public void controlRecieveMessage(ReplicaConnection replicaConnection, String umsg) {
		// TODO Auto-generated method stub
		
	}

}
