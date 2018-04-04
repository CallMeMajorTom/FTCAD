package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import both.Message;
import both.Worker;

// responsible for sending messages, indirectly receiving messages, ordering, diffusion, 
// acknowledge received messages
public class FEConnectionToClient {
	private final int mPort;
	private final InetAddress mAddress;
	private final DatagramSocket mSocket;
	private ArrayList<Message> mSentMessages = new ArrayList<Message>();
	private ArrayList<Message> mReceivedMessages = new ArrayList<Message>();
	private ArrayList<Message> mAcks = new ArrayList<Message>();
	private BlockingQueue<Message> mExpectedBQ = null;
	private int mExpected = 1; // current expected message
	private InetAddress mFEAddress;
	private int mFEPort;
	private boolean hasCrashed = false;
	long startTime;
	long endTime;
	long lengthTime;

	public FEConnectionToClient(InetAddress clientName, int ClientPort, InetAddress feAddress, int fePort,
			DatagramSocket fesocket, BlockingQueue<Message> ExpectedBQ) {
		this.mAddress = clientName;
		this.mPort = ClientPort;
		this.mFEAddress = feAddress;
		this.mFEPort = fePort;
		this.mSocket = fesocket;
		mExpectedBQ = ExpectedBQ;
	}

	// send message to client
	synchronized public void sendMessage(Message message) {
		startTime = System.currentTimeMillis();
		//System.out.println("starttime: "+startTime);
		message.setToPrimary(false);
		// convert message to bytearray
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream object_output = new ObjectOutputStream(outputStream);
			object_output.writeObject(message);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		byte[] data = outputStream.toByteArray();
		// send
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, mFEAddress, mFEPort);
		if (message.getMsgType()) {
			try {
				mSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			System.out.println("ack sent: " + sendPacket.getPort());
		} else {
			mSentMessages.add(message);
			new Thread(new Worker(sendPacket, mSocket, message)).start();
		}

		long endTime = System.nanoTime();
		//System.out.println("endtime: "+endTime);
		lengthTime = endTime - startTime;
	}

	/*
	 * void runperiodically(){ if (timenow-lasttime>60){ ///assume client as
	 * crashed hasCrashed = true ;
	 * 
	 * } }
	 */

	synchronized public void receiveMessage(Message message) {
		long lastTime = System.currentTimeMillis();
		//System.out.println("endtime: "+endTime);

		long currentTime = System.currentTimeMillis();
		long lengthTime = currentTime - lastTime;
		if (lengthTime > 5000) {
			System.out.println("Clinet has crashed \n System will exit");
			System.exit(-1);

		}
		if (message.getMsgType()) {// ack type.
			System.out.println("ack extracted from packet");
			try {
				searchMsgListById(mSentMessages, message.getID()).setConfirmedAsTrue();
			} catch (Exception e) {// cant happen or you didnt save whatever you sent. or a hacker. 
				//TODO investigate please!
				System.out.println("id: "+message.getID());
				System.out.print("list: ");
				for (Message each : mSentMessages) {
					System.out.print(each.getID() + " ");
				}
				e.printStackTrace();
				System.exit(-1);
			} 
				
		} else {// send type. record message and save ack. Ack should be sent
				// when sendAcks is called
			System.out.println("message extracted from packet");
			try {
				searchMsgListById(mSentMessages, message.getID());
			} catch (Exception e) {
				message.setMsgTypeAsTrue();
				message.setConfirmedAsTrue();
				message.setToPrimary(true);
				recordReceivedMessage(message);
			}
			try {
				searchMsgListById(mAcks, message.getID());
			} catch (Exception e) {
				message.setConfirmedAsTrue();
				mAcks.add(message);
			}
		}
	}

	synchronized public void sendAcks() {
		for (Iterator<Message> i = mAcks.iterator(); i.hasNext();) {
			Message msg = i.next();
			msg.setMsgTypeAsTrue();
			msg.setToPrimary(false);
			sendMessage(msg);
		}
		mAcks = new ArrayList<Message>();
	}

	public boolean compareClient(InetAddress address, int port) {
		return mPort == port && mAddress.equals(address);
	}

	private void recordReceivedMessage(Message message) {
		mReceivedMessages.add(message);
		if (mExpected == message.getID())
			produceExpected(message);
	}

	private void produceExpected(Message message) {
		try {
			mExpectedBQ.put(message);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			System.exit(-1);// TODO why did this happen?
		}
		// produce for server to consume
		mExpected++;
		// search receivedmessages for next expected one if found call it with
		// this function
		try {
			produceExpected(searchMsgListById(mReceivedMessages, mExpected));
		} catch (Exception e) {
		}
	}

	private Message searchMsgListById(ArrayList<Message> msgs, int id) throws Exception {
		for (Iterator<Message> i = msgs.iterator(); i.hasNext();) {
			Message msg = i.next();
			if (id == msg.getID())
				return msg;
		}
		throw new Exception();
	}

}
