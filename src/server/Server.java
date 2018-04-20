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
	protected static int mPrimary_Port = -1;// The port of the primary RM
	protected boolean mHoldingElection;
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
		m_state = new Undetermined();// the initial state is Undetermined state
	}

	private void addReplicas(int port) {
		boolean duplicate = false;
		for(ReplicaConnection check : mReplicaConnections) {
			if(check.mPort==port) {
				duplicate = true;
			}
		}
		if(mPort == port || duplicate) {
			System.err.println("duplicate rm detected");
			System.exit(-1);
		}
		ReplicaConnection rep = new ReplicaConnection(port,this, mTSocket);
		Thread thread = new Thread(rep);
		thread.start();
		mReplicaConnections.add(rep);
		System.out.println("replica connection ["+port+"] started");
	}

	//TODO is this used?
	private Thread mTalker = new Thread () { 
		public void run() {
			System.out.println("Talker running for " + mPort); 
			while (true) { 
				if(!isCoordinatorAlive()) holdElection(); 
				try { Thread.sleep(3000); 
				} catch (InterruptedException e) { e.printStackTrace(); } 
			} 
		}
		private boolean isCoordinatorAlive() {
			// TODO
			return false;
		} 
	};
	 
	//TODO is this used
	public void holdElection() { 
		System.out.println("P"+mPort+" starting an election"); 
		mHoldingElection = true;
		sendElectionMessageToPeers(); 
		try { Thread.sleep(1000); } catch(InterruptedException e) { e.printStackTrace(); }
		System.out.println("tsocket: "+mTSocket); 
		for(ListIterator<ReplicaConnection> itr = mReplicaConnections.listIterator();itr.hasNext();) { 
			int port = itr.next().mPort; 
			if (port > mPort) { 
				synchronized (pendingElecResps) {
					if (!pendingElecResps.containsKey(port)) { return; } 
				} 
			} 
		}
		System.out.println("P" + mPort + " set itself as coordinator");
		mPrimary_Port = mPort; 
		for(ListIterator<ReplicaConnection> itr = mReplicaConnections.listIterator();itr.hasNext();){
			//TODO inform everyone that you are the coordinator 
			//itr.next().sendMessage(Message.COORDINATOR);
		}
		mHoldingElection = false; 
	}
	
	//looks into primary file. ask everone if they are the primary.
	protected boolean isPrimaryAlive()  {
		// System.out.println(id + " checking if the coordinator is alive");//bad print
		XMLConfiguration conf = null;
		try {
			conf = new XMLConfiguration();
			conf.load("primary.xml");
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		int readPrimary = conf.getInt("port");
		//System.out.println("primary is: "+Primary_Port);//bad print
		if (mPrimary_Port == mPort && readPrimary == mPrimary_Port) {
			// if you are the coordinator, then you know you are alive
			return true;
		}else{
			if(mPrimary_Port == mPort && readPrimary != mPrimary_Port){
				System.err.println("This server think its the primary and the primary file is something else");
				System.exit(-1);
			}
			//TODO ask everone if they are the primary
			ArrayList<Integer> plist;//primarylist;
			if(plist.size()==1) {
				if (readPrimary != plist.get(0)) {
					System.err.println("primary isnt saved to file");
					//TODO maybe save primary to file?
				}
				mPrimary_Port = readPrimary;
				return true;
			}else if(plist.size()>1) {
				System.err.println("multiple primaries detected");
				System.exit(-1);
				return false;//TODO is this wrong?
			}else if(readPrimary == -1) {
				return false;
			}else return false
		}
	}

	//looks into primary file. ask everone if they are the primary and checks after deadline. 
	protected boolean isPrimaryAliveDeadline()  {
		// System.out.println(id + " checking if the coordinator is alive");//bad print
		XMLConfiguration conf = null;
		try {
			conf = new XMLConfiguration();
			conf.load("primary.xml");
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		int readPrimary = conf.getInt("port");
		//Primary_Port = conf.getInt("port");
		//System.out.println("primary is: "+Primary_Port);//bad print
		if (mPrimary_Port == mPort && readPrimary == mPrimary_Port) {
			// if you are the coordinator, then you know you are alive
			return true;
		} else if (readPrimary != -1) {
			if(mPrimary_Port == mPort && readPrimary != mPrimary_Port){
				System.err.println("This server think its the primary and the primary file is something else");
				System.exit(-1);
			}
			// ask the coordinator if he is alive with a ping
			sendPingToPeer(readPrimary);
			// wait for its response
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// if the coordinator did not reply with a pong in time, then assume its dead
			if (pendingPings.containsKey(mPrimary_Port)) {
				//System.out.println("Old Coordinator " + mPrimary_Port + " died");//bad print
				pendingPings.remove(mPrimary_Port);
				return false;
			}
			//TODO ask if coordinator is primary
			mPrimary_Port = readPrimary;
			return true;
		} else
			return false;
	}

	public void sendElectionMessageToPeers() {
		for (ListIterator<ReplicaConnection> itr = mReplicaConnections.listIterator(); itr.hasNext();) {
			ReplicaConnection rmc = itr.next();
			if (rmc.mPort > mPort && rmc.getAlive()) {
				try {
					RMmessage msg = new RMmessage(mPort,rmc.mPort,"ELECTION");
					rmc.sendMessage(msg);
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
			System.out.println("1.sends ping to: "+peer.mPort);
			if (peer.mPort == peerPort) {
				try {
					RMmessage msg = new RMmessage(mPort,peerPort,"PING");
                    synchronized (pendingPings) {
                        pendingPings.put(peerPort, true);
                        System.out.println("2.puts (port, true) into pendingpings map");
                    }
					peer.sendMessage(msg);
                    System.out.println("3.sent: " + msg.getType());
				} catch (Exception e) {}
			}
		}
	}
	
	//ask for update from primary. TODO returns what?
	public boolean askingForUpdate() {
		for (ListIterator<ReplicaConnection> itr = mReplicaConnections.listIterator(); itr.hasNext();) {
			ReplicaConnection rmc = itr.next();
			if (rmc.mPort == mPrimary_Port) {
				try {
					RMmessage msg = new RMmessage(mPort,rmc.mPort,"UPDATE");
					rmc.sendMessage(msg);
					Thread.sleep(500);
					System.out.println("sent update");
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
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
		if (!mHoldingElection) {// start voting_state;
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
		System.out.println("P" + mPort + " received ping from P" +  m.getSourcePort());
		for (ListIterator<ReplicaConnection> itr = mReplicaConnections.listIterator(); itr.hasNext();) {
			ReplicaConnection peer = itr.next();
			if (peer.mPort == m.getSourcePort()) {
				try {
					RMmessage msg = new RMmessage(mPort,peer.mPort,"PONG");
					peer.sendMessage(msg);
				} catch (Exception e) {
					System.err.println("Cant send for some reason");
				}
			}
		}
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
		return mPrimary_Port ;
	}
	
	synchronized public void controlRecieveMessage(RMmessage m) {// TODO:
		if (m.getType().equals("ELECTION")) {
			receiveElectionMessage(m);System.out.println("Receive Election");
		} else if (m.getType().equals("COORDINATOR")){
			receiveCoordinatorMessage(m);System.out.println("Receive Coordinator");
		} else if  (m.getType().equals("OK")){
			receiveOkMessage(m);System.out.println("Receive Ok");
		} else if (m.getType().equals("PING")){
			receivePingMessage(m);System.out.println("Receive Ping");
		} else if  (m.getType().equals("PONG")){
			receivePongMessage(m);System.out.println("Receive Pong");
		} else if  (m.getType().equals("UPDATE")){
			receiveUpdateMessage(m);System.out.println("Receive Update");
		}else {
			throw new RuntimeException("Unknown message type " + m);
		}
	}


}