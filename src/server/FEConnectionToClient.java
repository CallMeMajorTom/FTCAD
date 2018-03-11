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
public class FEConnectionToClient extends Thread {
	private final int mPort;
	private final InetAddress mAddress;
	private final DatagramSocket mSocket;
	private ArrayList<Message> mSentMessages = new ArrayList<Message>();
	private ArrayList<Message> mReceivedMessages = new ArrayList<Message>();
	private ArrayList<Message> mAcks = new ArrayList<Message>();
	private BlockingQueue<Message> mExpectedBQ = null;
	private int mExpected=1; //current expected message
	private boolean mAlive = true;

	public FEConnectionToClient(InetAddress clientName, int ClientPort, DatagramSocket fesocket, BlockingQueue<Message> ExpectedBQ) {
		this.mAddress = clientName;
		this.mPort = ClientPort;
		this.mSocket = fesocket;
		mExpectedBQ = ExpectedBQ;
	}
	
	public void run() {
		while(mAlive ) {}
	}

	// send message to client
	synchronized public void sendMessage(Message message){
		System.out.println("reply sent");
		mSentMessages.add(message);
		//convert message to bytearray
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream object_output = new ObjectOutputStream(outputStream);
			object_output.writeObject(message);
		} catch (IOException e) {
			e.printStackTrace(); System.exit(-1);
		}
		byte[] data = outputStream.toByteArray();
		//send
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, mAddress, mPort);
		new Thread(new Worker(sendPacket, mSocket, message)).start();
	}
	
	synchronized public void receiveMessage(Message message){
		if(message.getMsgType()){//ack type.
			try {searchMsgListById(mSentMessages, message.getID()).setConfirmedAsTrue();
			} catch (Exception e) {e.printStackTrace(); System.exit(-1);}//cant happen or you didnt save whatever you sent. or a hacker. TODO investigate please!
		}
		else {//send type. record message and save ack. Ack should be sent when sendAcks is called
			try {
				searchMsgListById(mSentMessages, message.getID());
			} catch (Exception e) {
				message.setMsgTypeAsTrue();
				message.setConfirmedAsTrue();
				recordReceivedMessage(message);
			}
			try {
				searchMsgListById(mAcks, message.getID());
			} catch (Exception e) {
				message.setMsgTypeAsTrue();
				message.setConfirmedAsTrue();
				mAcks.add(message);
			}
		}
	}
	
	synchronized public void sendAcks(){
		for(Iterator<Message> i = mAcks.iterator(); i.hasNext();  ) {
			sendMessage(i.next());
		}
		mAcks = new ArrayList<Message>();
	}
	
	public boolean compareClient(InetAddress address, int port) {
		return mPort == port && mAddress.equals(address);
	}

	private void recordReceivedMessage(Message message) {
		mReceivedMessages.add(message);
		if(mExpected == message.getID()) produceExpected(message);
	}
	
	private void produceExpected(Message message) {
		try {
			mExpectedBQ.put(message);
		} catch (InterruptedException e1) {
			e1.printStackTrace(); System.exit(-1);//TODO why did this happen?
		}
		//produce for server to consume
		mExpected++;
		//search receivedmessages for next expected one if found call it with this function
		try {produceExpected(searchMsgListById(mReceivedMessages, mExpected));} catch (Exception e) {}
	}
	
	private Message searchMsgListById(ArrayList<Message> msgs, int id) throws Exception{
		// TODO Auto-generated method stub
		for(Iterator<Message> i = msgs.iterator(); i.hasNext();  ) {
			Message msg = i.next();
			if(mExpected == msg.getID()) return msg;
		}
		throw new Exception();
	}

}
