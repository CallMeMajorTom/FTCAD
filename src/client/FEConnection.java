package client;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

import both.Message;

/**
 *
 * @author brom
 */
public class FEConnection {
	
    private DatagramSocket m_socket = null;
    private InetAddress m_FEAddress = null;
    private int m_FEPort = -1;
    
    public FEConnection(String hostName, int port) throws SocketException, UnknownHostException {
    	m_FEPort = port;
		m_FEAddress = InetAddress.getByName(hostName);
		m_socket = new DatagramSocket();
    }

    
    public Message receiveChatMessage() throws IOException, ClassNotFoundException {
    	byte[] incomingData = new byte[256];
    	DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
		ByteArrayInputStream byte_stream =new ByteArrayInputStream(incomingPacket.getData());
		ObjectInputStream object_stream = new ObjectInputStream(byte_stream);
		Message message = (Message)object_stream.readObject();
		return message;
    }
    
    public void sendChatMessage(Message message) throws IOException {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	ObjectOutputStream object_output = new ObjectOutputStream(outputStream);
    	object_output.writeObject(message);
    	byte[] data = outputStream.toByteArray();
    	DatagramPacket sendPacket = new DatagramPacket(data, data.length, m_FEAddress,m_FEPort);
    	m_socket.send(sendPacket);
    }

}
