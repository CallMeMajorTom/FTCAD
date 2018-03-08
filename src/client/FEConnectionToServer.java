package client;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import both.Message;
import both.Worker;

public class FEConnectionToServer extends  Thread{

	private DatagramSocket m_socket = null;
	private InetAddress m_FEAddress = null;
	private LinkedList<Message> mReceivedList = new LinkedList<Message>();
	private LinkedList<Message> mSendList = new LinkedList<Message>();
	private ArrayList<Message> mExpectedMessages = new ArrayList<Message>();
	private int m_FEPort = -1;
	private int mCSM=1; //current sent message
	private int mExpected = 1;//expected is the expected message ID to garantee order id
	private int mLock = 1;//lock means the element before lock is ordered

	public FEConnectionToServer(String hostName, int port) throws SocketException, UnknownHostException {
		m_FEPort = port;
		m_FEAddress = InetAddress.getByName(hostName);
		m_socket = new DatagramSocket(null);
    	m_socket.connect(m_FEAddress, m_FEPort);
	}

	public void run(){//Keep receive message
		Message message = null;
		while(true){
			message = receiveChatMessage();
			if(message.getMsgType()){//we got a ack
				try {
					searchMsgListById(mSendList, message.getID()).setConfirmedAsTrue();
				} catch (Exception e) {
					e.printStackTrace(); System.exit(-1);//cant happen or you didnt save whatever you sent. or a hacker. TODO investigate please!
				}
			}
			else {//we got a send. Operate the ordering
				if(message.getID() == mExpected){//receive the expected one
					mReceivedList.add(mExpected-1, message);
					for ( ListIterator<Message> itr = mReceivedList.listIterator();itr.hasNext();){
						if(itr.next().getID() == mExpected) mExpected++;
						else break;
					}
				}
				else{//receive unexpected message. find the proper position and insert
					ListIterator<Message> itr = mReceivedList.listIterator();
					//ListIterator<Integer> cur = i2.listIterator();
					while(true){
						if(itr.hasNext()) {
							if(itr.next().getID() > message.getID()) {
								itr.previous(); 
								itr.add(message); 
								break;
							}
						} else {itr.add(message);break;}
					}
				}
			}
		}
	}

	public Message receiveChatMessage(){
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

	public void sendChatMessage(Message message) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream object_output = null;
		try {
			object_output = new ObjectOutputStream(outputStream);
			object_output.writeObject(message);
		} catch (IOException e) {
			e.printStackTrace(); System.exit(-1);
		}
		byte[] data = outputStream.toByteArray();
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, m_FEAddress, m_FEPort);
		new Thread(new Worker(sendPacket,m_socket,message)).start();
		/*if (message.getMsgType()) {
			new Thread(
				new Runnable() {
					public void run() {
						boolean confirmed = false;
						byte[] data = outputStream.toByteArray();
						DatagramPacket sendPacket = new DatagramPacket(data, data.length, m_FEAddress, m_FEPort);
						while (!message.getConfirmed()) {
							int count = 10;
							while (count-- > 0) {//diffusion
								try {
									m_socket.send(sendPacket);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							try {
								Thread.sleep(15);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				});
		}*/
	}

	public int getExpected(){
		return mExpected;
	}

	public int getLock(){
		return mLock;
	}

	public int increaseExpected(){
		return mExpected++;
	}

	public void removeFirst(){
		mReceivedList.removeFirst();
	}

	public Message getMsg(int ID){
		for ( ListIterator<Message> itr = mReceivedList.listIterator();itr.hasNext();){
			if(itr.next().getID() == ID) return itr.next();
		}
		return null;
	}
	
	public void receiveMessage(Message message){
		if (receiveDiffusion()){
			if(!message.getMsgType()) {//record and send acknowledge
				message.setMsgTypeAsTrue();
				recordMessage(message);
				sendChatMessage(message);
			}
		}
	}

	private boolean receiveDiffusion() {
		// if first time of this message returns true otherwise false
		return true;
	}

	private void recordMessage(Message message) {
		mReceivedList.add(message);
		if(mExpected == message.getID()) produceExpected(message);
	}
	
	private void produceExpected(Message message) {
		mExpectedMessages.add(message);
		//produce for server to consume
		mExpected++;
		//search receivedmessages for next expected one if found call it with this function
		try {produceExpected(searchMsgListById(mReceivedList, mExpected));} catch (Exception e) {}
	}
	
	private Message searchMsgListById(LinkedList<Message> msgs, int id) throws Exception{
		// TODO Auto-generated method stub
		for(Iterator<Message> i = msgs.iterator(); i.hasNext();  ) {
			Message msg = i.next();
			if(mExpected == msg.getID()) return msg;
		}
		throw new Exception();
	}
}
