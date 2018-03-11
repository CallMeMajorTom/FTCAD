package server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

import both.Message;

public class Server {
	private ArrayList<FEConnectionToClient> mFEConnectionToClients = new ArrayList<FEConnectionToClient>();//array list of Clients
	private ArrayList<ReplicaConnection> mReplicaConnections = new ArrayList<ReplicaConnection>();
	private ServerSocket mTSocket;
	private DatagramSocket mUSocket;
	private final int mPort;
	private final String mAddress = "localhost";
	private boolean mPrimary;//To show if this process is primary server
	private boolean holdingElection;
	private int primary = -1;//The id of primary server
	private Map<Integer, Boolean> pendingPings = new HashMap<Integer, Boolean>();//Waiting for the reply of the Ping
	private Map<Integer, Boolean> pendingElecResps = new HashMap<Integer, Boolean>();//Waiting for the reply of the Election

	private Thread mTalker = new Thread () {
		public void run() {
			System.out.println("Talker running for " + mPort);
			while (true) {
				if (!isCoordinatorAlive()) {
					holdElection();
				}
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

	};

	public void holdElection() {
		System.out.println("P" + mPort + " starting an election");
		holdingElection = true;
		sendElectionMessageToPeers();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//System.out.println(sockets);
		for(ListIterator<ReplicaConnection> itr = mReplicaConnections.listIterator();itr.hasNext();) {
			int port = itr.next().mPort;
			if (port > mPort) {
				synchronized (pendingElecResps) {
					if (!pendingElecResps.containsKey(port)) {
						return;
					}
				}
			}
		}
		System.out.println("P" + mPort + " set itself as coordinator");
		this.primary = mPort;
		for(ListIterator<ReplicaConnection> itr = mReplicaConnections.listIterator();itr.hasNext();){//inform everyone that you are the coordinator
			itr.next().sendMessage(Message.COORDINATOR);
		}
		holdingElection = false;
	}

	private boolean isCoordinatorAlive(){
		// System.out.println(id + " checking if the coordinator is alive");
		if (primary == mPort) {
			// if you are the coordinator, then you know you are alive
			return true;
		}
		else if (primary != -1) {
			// ping the coordinator
			sendPingToPeer(primary);
			// wait for its response
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// if the coordinator did not reply with a pong, the its dead
			if (pendingPings.containsKey(primary)) {
				System.out.println("Old Coordinator " + primary + " died");
				pendingPings.remove(primary);
				return false;
			}
			return true;
		}
		primary = mPort;
		return true;
	}

	public void sendElectionMessageToPeers() {
		for(ListIterator<ReplicaConnection> itr = mReplicaConnections.listIterator();itr.hasNext();) {
			int port = itr.next().mPort;
			if (port > mPort){
				itr.next().sendMessage(Message.ELECTION);
				synchronized (pendingElecResps) {
					pendingElecResps.put(port, false);
				}
			}
		}
	}


	public void sendPingToPeer(int peerPort) {
		for(ListIterator<ReplicaConnection> itr = mReplicaConnections.listIterator();itr.hasNext();){
			ReplicaConnection peer = itr.next();
			if(peer.mPort == peerPort)
				peer.sendMessage(Message.PING);
		}
		synchronized (pendingPings) {
			pendingPings.put(peerPort, true);
		}
	}


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

	private Server(int Port, int FEPort){
			mPort = Port;
			mFEPort = FEPort;
			try {
				mTSocket = new ServerSocket(mPort);
				mUSocket = new DatagramSocket(mPort);
			} catch (IOException e) {e.printStackTrace(); System.exit(-1);}
			mPrimary = false;
			mTalker.start();
	}

	private void addReplicas(int port) {
		ReplicaConnection rep = new ReplicaConnection(port, this, mTSocket);
		Thread thread = new Thread(rep);
		thread.start();
		mReplicaConnections.add(rep);
	}

	private void init() {
		do {
			//if(-1 == whoIsPrimary()) election(); else join();
			if(mPrimary)actAsPrimary(); else actAsBackup();
		} while(true);
	}

	private void actAsBackup() {
		//TODO
	}

	private void actAsPrimary() {
		//TODO
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
		InetAddress naddress = InetAddress.getByName(mAddress);
		ClientConnection m_ClientConnection = new ClientConnection(naddress, port, mUSocket);
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

	synchronized public void controlRecieveMessage(ReplicaConnection replicaConnection, String m) {
		if(m.equals(Message.ELECTION))
			receiveElectionMessage(m);
		else if(m.equals(Message.COORDINATOR))
			receiveCoordinatorMessage(m);
		else if(m.equals(Message.OK))
			receiveOkMessage(m);
		else if(m.equals(Message.PING))
			receivePingMessage(m);
		else if(m.equals(Message.PONG))
			receivePongMessage(m);
		else
			throw new RuntimeException("Unknown message type " + m);
	}

}