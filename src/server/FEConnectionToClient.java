package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;

import both.Message;
import both.Worker;

// responsible for sending messages, indirectly receiving messages, ordering, diffusion, 
// acknowledge received messages
public class FEConnectionToClient extends Thread {

	private final DatagramSocket mSocket;
	private final int mPort;
	
	private InetAddress mAddress = null;
	private ArrayList<Message> mSentMessages = new ArrayList<Message>();
	private ArrayList<Message> mReceivedMessages = new ArrayList<Message>();
	private int mCSM=1; //current sent message
	private int mCEM=1; //current expected message
	private boolean mAlive = true;

	// Constructor
	public FEConnectionToClient(InetAddress clientName, int ClientPort, DatagramSocket fesocket) {
		this.mAddress = clientName;
		this.mPort = ClientPort;
		this.mSocket = fesocket;
	}

	// send message to client
	public void sendMessage(Message message){
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
	
	public void receiveMessage(Message message){
		if (receiveDiffusion()){
			if(!message.getMsgType()) {//record and send acknowledge
				message.setMsgTypeAsTrue();
				recordMessage(message);
				sendMessage(message);
			}
		}
	}

	private boolean receiveDiffusion() {
		// if first time of this message returns true otherwise false
		return true;
	}

	private void recordMessage(Message message) {
		mReceivedMessages.add(message);
		if(mCEM == message.getID()) produceExpected(message);
	}
	
	private void produceExpected(Message message) {
		//produce for server to consume
		mCEM++;
		//search receivedmessages for next expected one if found call it with this function
		try {produceExpected(searchMsgListById(mReceivedMessages, mCEM));} catch (Exception e) {}
	}
	
	private Message searchMsgListById(ArrayList<Message> msgs, int id) throws Exception{
		// TODO Auto-generated method stub
		for(Iterator<Message> i = mReceivedMessages.iterator(); i.hasNext();  ) {
			Message msg = i.next();
			if(mCEM == msg.getID()) return msg;
		}
		throw new Exception();
	}

	public void run() {
		while(mAlive ) {}
	}

}
