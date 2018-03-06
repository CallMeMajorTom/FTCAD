package server;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import both.Message;
import both.Worker;

/**
 * 
 * @author brom
 */
// Mainly responsible for sending messages
public class ClientConnection extends Thread {

	private DatagramSocket m_socket = null;
	private InetAddress m_ClientAddress = null;
	private int m_ClientPort = -1;
	private ArrayList<Integer> portList = new ArrayList<Integer>();
	private ArrayList<InetAddress> sendAddressList = new ArrayList<InetAddress>();
	private ArrayList<DatagramSocket> sendSocketList = new ArrayList<DatagramSocket>();
	private ArrayList<Message> mSentMessages = new ArrayList<Message>();
	private ArrayList<Message> mReceiveMessages = new ArrayList<Message>();

	// Constructor
	public ClientConnection(String hostName, int ClientPort) throws SocketException, UnknownHostException {
		this.m_ClientAddress = InetAddress.getByName(hostName);
		addToSendAddressList();
		this.m_ClientPort = ClientPort;
		addToSendListPort();
		this.m_socket = new DatagramSocket();
		addToSocketList();
	}

	public void setmSentMessages(ArrayList<Message> mSentMessages) {
		this.mSentMessages = mSentMessages;
	}

	public void setmReceiveMessages(ArrayList<Message> mReceiveMessages) {
		this.mReceiveMessages = mReceiveMessages;
	}

	// send message to client
	public void sendMessage(Message message) throws IOException {
		System.out.println("reply sent");
		getSentMessages()
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream object_output = new ObjectOutputStream(outputStream);
		object_output.writeObject(message);
		byte[] data = outputStream.toByteArray();
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, m_ClientAddress, m_ClientPort);
		new Thread(new Worker(sendPacket, m_socket, message)).start();

	}

	public void run() {
	}

	// adding to lists
	public void addToSendListPort() {
		portList.add(m_ClientPort);

	}

	// continue...
	public void addToSendAddressList() {
		sendAddressList.add(m_ClientAddress);
	}

	public void addToSocketList() {
		sendSocketList.add(m_socket);
	}

	// getters and setters
	public ArrayList<Integer> getPortList() {
		return portList;
	}

	// continued...
	public ArrayList<InetAddress> getSendAddressList() {
		return sendAddressList;
	}

	public ArrayList<DatagramSocket> getSendSocketList() {
		return sendSocketList;
	}

	public void setPortList(ArrayList<Integer> portList) {
		this.portList = portList;
	}

	public void setSendAddressList(ArrayList<InetAddress> sendAddressList) {
		this.sendAddressList = sendAddressList;
	}

	public void setSendSocketList(ArrayList<DatagramSocket> sendSocketList) {
		this.sendSocketList = sendSocketList;
	}

	public ArrayList<Message> getSentMessages() {
		return mSentMessages;
	}

	public ArrayList<Message> getReceiveMessages() {
		return mReceiveMessages;
	}

}
