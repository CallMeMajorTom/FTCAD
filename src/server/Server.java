package server;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import both.Message;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

public class Server {

	protected ArrayList<FEConnectionToClient> mFEConnectionToClients = new ArrayList<FEConnectionToClient>();//The array list of Clients
	protected static ArrayList<ReplicaConnection> mReplicaConnections = new ArrayList<ReplicaConnection>();// The array list of
	protected static int Primary_Port = -1;// The port of the primary RM
	protected boolean holdingElection;
	protected final int mPort;// The port of THIS server
	protected final String mAddress = "localhost";//The address of THIS server
	protected InetAddress mFEAddress;
	protected Map<Integer, Boolean> pendingPings = new HashMap<Integer, Boolean>();// The waiting list for the reply of PING message
	protected Map<Integer, Boolean> pendingElecResps = new HashMap<Integer, Boolean>();// The waiting list for the reply of ELECTION message
	protected ArrayList<Message> mMessageList = new ArrayList<Message>();//The list of the message, to record all operation
	protected BlockingQueue<Message> mExpectedBQ = new LinkedBlockingQueue<Message>();
	protected int mMsgID = 1;
	protected DatagramSocket mUSocket;// The Socket for communication between Server and Client
	protected final int mFEPort;// The Port of the Frontend
	private ServerSocket mTSocket;// The Socket for communication between RM
	private State m_state;// The State, including : crashed, undetermined, voting, backup(no_integrated), backup(integrated), primary

	private void StateMachine(){
			System.out.println("State machine running for " + mPort);
			while (true) {
				m_state = m_state.update(this);
			}
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Need both port and FEport");
			System.exit(-1);
		}
		int Port = Integer.parseInt(args[0]);
		int FEPort = Integer.parseInt(args[1]);
		Server instance = new Server(Port, FEPort);
		for (int i = 2; i < args.length; i++)
			instance.addReplicas(Integer.parseInt(args[i]));
		instance.StateMachine();
	}

	private Server(int Port, int FEPort) {
		System.out.println("Port: "+Port);
		mPort = Port;
		mFEPort = FEPort;
		try {
			mTSocket = new ServerSocket(mPort);
			mUSocket = new DatagramSocket(mPort);
		} catch (Exception e) {
			e.printStackTrace();System.exit(-1);
		}
		try {
			mFEAddress = InetAddress.getByName(mAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace(); System.exit(-1);
		}
		System.out.println("mUSocket port: "+mUSocket.getLocalPort());
		m_state = new Undetermined();// the initial state is Undetermined state
	}

	private void addReplicas(int port) {
		ReplicaConnection rep = new ReplicaConnection(port,this, mTSocket);
		Thread thread = new Thread(rep);
		thread.start();
		mReplicaConnections.add(rep);
		System.out.println("replica connection ["+port+"] started");
	}

	/*
	 * private Thread mTalker = new Thread () { public void run() {
	 * System.out.println("Talker running for " + mPort); while (true) { if
	 * (!isCoordinatorAlive()) { holdElection(); } try { Thread.sleep(3000); }
	 * catch (InterruptedException e) { e.printStackTrace(); } } } };
	 */

	/*
	 * public void holdElection() { System.out.println("P" + mPort +
	 * " starting an election"); holdingElection = true;
	 * sendElectionMessageToPeers(); try { Thread.sleep(1000); } catch
	 * (InterruptedException e) { e.printStackTrace(); }
	 * //System.out.println(sockets); for(ListIterator<ReplicaConnection> itr =
	 * mReplicaConnections.listIterator();itr.hasNext();) { int port =
	 * itr.next().mPort; if (port > mPort) { synchronized (pendingElecResps) {
	 * if (!pendingElecResps.containsKey(port)) { return; } } } }
	 * System.out.println("P" + mPort + " set itself as coordinator");
	 * this.primary = mPort; for(ListIterator<ReplicaConnection> itr =
	 * mReplicaConnections.listIterator();itr.hasNext();){//inform everyone that
	 * you are the coordinator itr.next().sendMessage(Message.COORDINATOR); }
	 * holdingElection = false; }
	 */

	protected boolean isPrimaryAlive()  {
		// System.out.println(id + " checking if the coordinator is alive");
		XMLConfiguration conf = null;
		try {
			conf = new XMLConfiguration();
			conf.load("primary.xml");
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		Primary_Port = conf.getInt("port");
		System.out.println(conf.getInt("port"));
		System.out.println("!!!"+Primary_Port+"!!!");
		if (Primary_Port == mPort) {
			// if you are the coordinator, then you know you are alive
			return true;
		} else if (Primary_Port != -1) {
			// ping the coordinator
			sendPingToPeer(Primary_Port);
			// wait for its response
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// if the coordinator did not reply with a pong, the its dead
			if (pendingPings.containsKey(Primary_Port)) {
				System.out.println("Old Coordinator " + Primary_Port + " died");
				pendingPings.remove(Primary_Port);
				return false;
			}
			return true;
		} else
			return false;
	}

	public void sendElectionMessageToPeers() {
		for (ListIterator<ReplicaConnection> itr = mReplicaConnections.listIterator(); itr.hasNext();) {
			ReplicaConnection rmc = itr.next();
			if (rmc.mPort > mPort && rmc.getAlive()) {
				try {
					rmc.sendMessage(RMmessage.ELECTION);
				} catch (Exception e) {}
				synchronized (pendingElecResps) {
					pendingElecResps.put(rmc.mPort, false);
				}
			}
		}
	}

	public void sendPingToPeer(int peerPort) {
		for (ListIterator<ReplicaConnection> itr = mReplicaConnections.listIterator(); itr.hasNext();) {
			ReplicaConnection peer = itr.next();
			System.out.println(peer.mPort);
			if (peer.mPort == peerPort) {
				try {
					peer.sendMessage(RMmessage.PING);
					System.out.println("Last");
				} catch (Exception e) {}
			}
		}
		synchronized (pendingPings) {
			pendingPings.put(peerPort, true);
			System.out.println("put");
		}
	}

	public boolean askingForUpdate() {
		for (ListIterator<ReplicaConnection> itr = mReplicaConnections.listIterator(); itr.hasNext();) {
			ReplicaConnection rmc = itr.next();
			if (rmc.mPort == Server.Primary_Port) {
				try {
					rmc.sendMessage(RMmessage.UPDATE);
					Thread.sleep(500);
					System.out.println("sent updata");
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public void  receiveUpdateMessage(RMmessage m){
		System.out.println("Port "+mPort + " received UPDATE from Port " + m.getSourcePort());
		for (ListIterator<ReplicaConnection> itr = mReplicaConnections.listIterator(); itr.hasNext();) {
			ReplicaConnection peer = itr.next();
			if (peer.mPort == m.getSourcePort()) {
				try {
					peer.sendList(mMessageList);
				} catch (Exception e) {
					System.err.println("Cant send for some reason");
				}
			}
		}
	}

	private void sendOkMessageToPeer(int sourcePort) {
	}

	private void sendMessageToPeer(int sourcePort, String pong, int i) {
	}

	public void receivePongMessage(RMmessage m) {
		System.out.println("Port "+mPort + " received pong from Port " + m.getSourcePort());
		synchronized (pendingPings) {
			if (pendingPings.containsKey(m.getSourcePort())) {
				pendingPings.remove(m.getSourcePort());
			}
		}
	}

	// Process a message when a peer says okay
	public void receiveOkMessage(RMmessage m) {
		System.out.println("P" + mPort + " received ok message from P" + m.getSourcePort());
		synchronized (pendingElecResps) {
			if (pendingElecResps.containsKey(m.getSourcePort())) {
				pendingElecResps.remove(m.getSourcePort());
			}
		}

	}

	// TODO: Put it in Backup state
	public void receiveElectionMessage(RMmessage m) {
		System.out.println("P" + mPort + " received election message from P" + m.getSourcePort());
		if (m.getSourcePort() < mPort) {// Send ok if the sender cant bully you
			sendOkMessageToPeer(m.getSourcePort());
		}
		if (!holdingElection) {// start voting_state;
			m_state = new Voting();
		}
	}

	// Receive information from the new coordinator
	/*
	 * public void receiveCoordinatorMessage(Message m) { System.out.println("P"
	 * + id + " received coordinator message from P" + m.getSourceId()); int
	 * coord = (Integer) m.getData().get(0); this.coordinator = coord; }
	 */

	// Receive a ping and send a pong
	public void receivePingMessage(RMmessage m) {
		// System.out.println("P" + id + " received ping from P" +
		// m.getSourceId());
		sendMessageToPeer(m.getSourcePort(), RMmessage.PONG, 0);
	}

	private void receiveCoordinatorMessage(RMmessage m) {
	}


	/*
	 * private int whoIsPrimary() { int winner = -1; ReplicaConnection c;
	 * for(Iterator<ReplicaConnection> i = mReplicaConnections.iterator();
	 * i.hasNext();) { c=i.next(); c.sendMessage("whoIsPrimary"); int r =
	 * c.receiveMessage2(); if(-1!=r) {winner = r; break;} } return winner; }
	 */

	/*private void listenForClientMessages() {
		System.out.println("Waiting for client messages... ");
		do {
			byte[] incomingData = new byte[256];
			DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
			ByteArrayInputStream byte_stream = new ByteArrayInputStream(incomingPacket.getData());
			try {
				ObjectInputStream object_stream = new ObjectInputStream(byte_stream);
				Message message = (Message) object_stream.readObject();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		} while (true);
	}*/

	/*public synchronized void broadcast(Message message) throws IOException {
		for (FEConnectionToClient cc : mFEConnectionToClients) {
			cc.sendMessage(message);
		}
	}*/

	public static int getReplicaConnections(){
		return mReplicaConnections.size() ;
	}
	
	public static int getCurrentServerReplica()
	{
		return Primary_Port ;
	}
	
	synchronized public void controlRecieveMessage(ReplicaConnection replicaConnection, RMmessage m) {// TODO:
		if (m.equals(RMmessage.ELECTION)) {
			receiveElectionMessage(m);
		} else if (m.equals(RMmessage.COORDINATOR))
			receiveCoordinatorMessage(m);
		else if (m.equals(RMmessage.OK))
			receiveOkMessage(m);
		else if (m.equals(RMmessage.PING))
			receivePingMessage(m);
		else if (m.equals(RMmessage.PONG))
			receivePongMessage(m);
		else if (m.equals(RMmessage.UPDATE)){
			receiveUpdateMessage(m);
		}
		else {
			throw new RuntimeException("Unknown message type " + m);
		}
	}


}