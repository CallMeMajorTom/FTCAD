package server;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import both.Message;

//responsible for receiving messages from the client
public class ListenerThread extends Thread {
	private ArrayList<FEConnectionToClient> mClientConnections;
	private BlockingQueue<Message> mClientMsgs;
	private DatagramSocket mUSocket;

	// Construtor
	public ListenerThread(ArrayList<FEConnectionToClient> clientConnections, BlockingQueue<Message> clientMsgs, 
			DatagramSocket USocket) {
		mClientConnections = clientConnections;
		mClientMsgs = clientMsgs;
		mUSocket = USocket;
	}

	// keep receving messages until the socket is closed
	public void run() {
		while (true) {
            try {
	        	//receive
	    		byte[] buf = new byte[256];
	    		DatagramPacket received = new DatagramPacket(buf, buf.length);
	            mUSocket.receive(received);
	            //convert to msg
	    		ByteArrayInputStream byte_stream = new ByteArrayInputStream(buf);
	    		ObjectInputStream object_stream = new ObjectInputStream(byte_stream);
	    		Message msg = (Message)object_stream.readObject();
				System.out.println("message recevied" + msg);
	    		//check if client exist. if not create client
				boolean addClient = false;
				FEConnectionToClient ctc = null;
				for (Iterator<FEConnectionToClient> i = mClientConnections.iterator(); i.hasNext();) {
					FEConnectionToClient ctci = i.next();
					if(ctci.compareClient(msg.getClient(), msg.getPort())) {
						ctc = ctci;
						addClient = true;
						break;
					}
				}
				if(!addClient) { 
					ctc = new FEConnectionToClient(msg.getClient(), msg.getPort(), mUSocket, mClientMsgs);
					mClientConnections.add(ctc);
				}
				ctc.receiveMessage(msg);
			} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
			}
		}
	}

}
