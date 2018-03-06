package client;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.ListIterator;

import both.Message;
import both.Worker;

/**
 *
 * @author brom
 */
public class FEConnection extends  Thread{

	private DatagramSocket m_socket = null;
	private InetAddress m_FEAddress = null;
	private LinkedList<Message> ReceivedList = new LinkedList<Message>();
	private LinkedList<Message> SendList = new LinkedList<Message>();
	private int m_FEPort = -1;
	private int expected = 1;//expected is the expected message ID to garantee order id
	private int lock = 1;//lock means the element before lock is ordered

	public FEConnection(String hostName, int port) throws SocketException, UnknownHostException {
		m_FEPort = port;
		m_FEAddress = InetAddress.getByName(hostName);
		m_socket = new DatagramSocket();
	}

	public void run(){//Keep receive message
		Message message = null;
		 while(true){
			 try {
				 message = receiveChatMessage();
			 } catch (IOException e) {
				 e.printStackTrace();
			 } catch (ClassNotFoundException e) {
				 e.printStackTrace();
			 }
			 if(!message.getMsgType()){//Operate the ack
				message.worker.setAck();
			 }
			 else {//Operate the ordering
			 	if(message.getID() == expected){//receive the expected one
					ReceivedList.addFirst(message);
					for ( ListIterator<Message> itr = ReceivedList.listIterator();itr.hasNext();){
						if(itr.next().getID() == lock) lock++;
						else break;
					}
				}
				else{//receive unexpected message
					ListIterator<Message> itr = ReceivedList.listIterator();
					for ( ;itr.hasNext(););
					while(itr.hasPrevious()){//find the proper position and insert
						if(itr.next().getID() > message.getID()) continue;
						else itr.add(message);
					}
				}
			 }
		 }
	}

	public Message receiveChatMessage() throws IOException, ClassNotFoundException {
		byte[] incomingData = new byte[256];
		DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
		ByteArrayInputStream byte_stream = new ByteArrayInputStream(incomingPacket.getData());
		ObjectInputStream object_stream = new ObjectInputStream(byte_stream);
		Message message = (Message) object_stream.readObject();
		return message;
	}

	public void sendChatMessage(Message message) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream object_output = null;
		object_output = new ObjectOutputStream(outputStream);
		object_output.writeObject(message);
		byte[] data = outputStream.toByteArray();
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, m_FEAddress, m_FEPort);
		message.worker = new Worker(sendPacket,m_socket,false);
		message.worker.start();
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
		return expected;
	}

	public int getLock(){
		return lock;
	}

	public int increaseExpected(){
		return expected++;
	}

	public void removeFirst(){
		ReceivedList.removeFirst();
	}

	public Message getMsg(int ID){
		for ( ListIterator<Message> itr = ReceivedList.listIterator();itr.hasNext();){
			if(itr.next().getID() == ID) return itr.next();
		}
		return null;
	}
}
