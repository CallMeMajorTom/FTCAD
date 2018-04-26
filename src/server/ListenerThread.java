package server;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import both.Message;

//responsible for receiving messages from the client
public class ListenerThread extends Thread {
	private ArrayList<FEConnectionToClient> mClientConnections;
	private BlockingQueue<Message> mClientMsgs;
	private DatagramSocket mUSocket;
	private int mFEPort;
	private InetAddress mFEAddress;
	private ArrayList<Message> mServerMsgs;

	// Construtor
	public ListenerThread(ArrayList<FEConnectionToClient> clientConnections, BlockingQueue<Message> clientMsgs, 
			DatagramSocket USocket, InetAddress feAddress, int fePort, ArrayList<Message> serverMsgs) {
		mFEPort = fePort;
		mFEAddress = feAddress;
		mClientConnections = clientConnections;
		mClientMsgs = clientMsgs;
		mServerMsgs = serverMsgs;
		mUSocket = USocket;
	}

	// keep receving messages until the socket is closed
	public void run() {
		System.out.println("listener started. Port: "+mUSocket.getLocalPort());
		while (true) {
            try {
	        	//receive
	    		byte[] buf = new byte[256*4];
	    		DatagramPacket received = new DatagramPacket(buf, buf.length);
	            mUSocket.receive(received);
	            //convert to msg
	    		ByteArrayInputStream byte_stream = new ByteArrayInputStream(buf);
	    		ObjectInputStream object_stream = new ObjectInputStream(byte_stream);
	    		Message msg = (Message)object_stream.readObject();
	    		System.out.println("packet received: "+ msg.getPort()+", "+msg.getID());
	    		//check if client exist. if not create client
				boolean addClient = true;
				FEConnectionToClient ctc = null;
				for (Iterator<FEConnectionToClient> i = mClientConnections.iterator(); i.hasNext();) {
					FEConnectionToClient ctci = i.next();
					if(ctci.compareClient(msg.getClient(), msg.getPort())) {
						ctc = ctci;
						addClient = false;
						break;
					}
				}
				if(addClient) { 
					ctc = new FEConnectionToClient(msg.getClient(), msg.getPort(), mFEAddress, mFEPort, mUSocket, mClientMsgs);
					for(Message smsg : mServerMsgs)
						ctc.sendMessage(smsg);
					mClientConnections.add(ctc);
				}
				//give connectiontoclient the message so he can produce expected or save it
				ctc.receiveMessage(msg);
			} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
			}
		}
	}
}
