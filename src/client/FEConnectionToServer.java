package client;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

import both.Message;
import both.Worker;

public class FEConnectionToServer extends  Thread{
	
	protected BlockingQueue<Message> mExpectedBQ = null;//use take() to consume msgs
	private DatagramSocket m_socket = null;
	private InetAddress m_FEAddress = null;
	private LinkedList<Message> mReceivedList = new LinkedList<Message>();
	private LinkedList<Message> mSendList = new LinkedList<Message>();
	private int m_FEPort = -1;
	private int mExpected = 1;//expected is the expected message ID to garantee order id of the BQ

	public FEConnectionToServer(String hostName, int port, BlockingQueue<Message> ExpectedBQ) throws SocketException, UnknownHostException {
		m_FEPort = port;
		m_FEAddress = InetAddress.getByName(hostName);
		m_socket = new DatagramSocket(null);
    	m_socket.connect(m_FEAddress, m_FEPort);
    	mExpectedBQ = ExpectedBQ;
	}

	public void run(){//Keep receive message
		Message message = null;
		while(true){
			message = receiveChatMessage();
			if(message.getMsgType()){//we got a ack
				try {
					searchMsgListById(mSendList, message.getID()).setConfirmedAsTrue();
				} catch (Exception e) {
					e.printStackTrace(); System.exit(-1);
				}//cant happen or you didnt save whatever you sent. or a hacker. TODO investigate please!
			}
			else {//we got a send. Operate the ordering. send a acknowledge
				try {//search if we already have the message
					searchMsgListById(mReceivedList, message.getID());
				} catch (Exception e) {//didnt have the message
					recordReceivedMessage(message);
				}
				message.setMsgTypeAsTrue();
				message.setConfirmedAsTrue();
				sendChatMessage(message);
			}
		}
	}
	
	public void sendChatMessage(Message message) {
		mSendList.add(message);
		//convert message to bytearray
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream object_output = null;
		try {
			object_output = new ObjectOutputStream(outputStream);
			object_output.writeObject(message);
		} catch (IOException e) {
			e.printStackTrace(); System.exit(-1);
		}
		byte[] data = outputStream.toByteArray();
		//send
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, m_FEAddress, m_FEPort);
		new Thread(new Worker(sendPacket,m_socket,message)).start();
	}
	
	private Message receiveChatMessage(){
		byte[] incomingData = new byte[256];
		DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
		ByteArrayInputStream byte_stream = new ByteArrayInputStream(incomingPacket.getData());
		ObjectInputStream object_stream = null;
		Message message = null;
		try {
			object_stream = new ObjectInputStream(byte_stream);
			message = (Message) object_stream.readObject();
		} catch (Exception e) {
			e.printStackTrace(); System.exit(-1);//should not happen because dont know why it would happen. TODO investigate please!
		}
		return message;
	}
	
	private void recordReceivedMessage(Message message) {
		mReceivedList.add(message);
		if(message.getID() == mExpected)//receive the expected one
			produceExpected(message);
	}
	
	private void produceExpected(Message message) {
		try {
			mExpectedBQ.put(message);
		} catch (InterruptedException e1) {
			e1.printStackTrace(); System.exit(-1);//TODO why did this happen?
		}
		//produce for server to consume
		mExpected++;
		//search received messages for next expected one if found call it with this function
		try {
			produceExpected(searchMsgListById(mReceivedList, mExpected));
		} catch (Exception e) {}
	}
	
	private Message searchMsgListById(LinkedList<Message> msgs, int id) throws Exception{
		for(Iterator<Message> i = msgs.iterator(); i.hasNext();  ) {
			Message msg = i.next();
			if(id == msg.getID()) return msg;
		}
		throw new Exception();
	}
}
