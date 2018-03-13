package frontEnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.XMLConfiguration;
import both.Message;

public class FrontEnd {
    private DatagramSocket mSocket;
	public final InetAddress mPrimaryAddress;
	public int mPrimaryPort=-1;
	private DatagramSocket mSocket2;

    public static void main(String[] args) {//starts constructor and do a handshake
        if (args.length < 1) {
            System.err.println("Usage: java FrontEnd portnumber");
            System.exit(-1);
        }
        FrontEnd instance = null;
        try {
            instance = new FrontEnd(Integer.parseInt(args[0]));
        } catch (NumberFormatException e) {
            System.err.println("Error: port number must be an integer.");
            System.exit(-1);
        }
        instance.listenAndSend();
    }
    
    private FrontEnd(int portNumber) {//initialize variables
    	InetAddress pa = null;
    	try {
    		mSocket = new DatagramSocket(portNumber);
    		mSocket2 = new DatagramSocket(null);
			pa = InetAddress.getByName("localhost");
    	} catch (Exception e) {
    		e.printStackTrace(); 
    		System.exit(-1);
    	}
    	mPrimaryAddress = pa;
    	mPrimaryPort = readfile();//for testing only
		System.out.println("Primary port: "+mPrimaryPort);
		System.out.println("mUSocket port: "+mSocket.getLocalPort());
    }

    private void listenAndSend() {
        do {
            try {
	        	//listens and receives a packet from a socket
	    		byte[] buf = new byte[256*4];
	    		DatagramPacket received = new DatagramPacket(buf, buf.length);
	            mSocket.receive(received);
	            //convert buf to object. convert object to a msg.
	    		ByteArrayInputStream byte_stream = new ByteArrayInputStream(buf);
	    		ObjectInputStream object_stream = new ObjectInputStream(byte_stream);
	    		Message msg = (Message)object_stream.readObject();
	    		//determine what to do with the msg
	    		//System.err.println(received.getPort()+", "+msg.getID());
	            if(!readPrimary(msg)) send(msg);
	    	} catch (Exception e) {
	    		e.printStackTrace(); 
	    		System.exit(-1);
	    	}
        } while (true);
    }
    
    private boolean readPrimary(Message msg){
		if(msg.getCommand().equals("tell")) {
			mPrimaryPort = readfile();
			return true;
		}else return false;
    }
    
    private int readfile() {
		XMLConfiguration conf = null;
		try {
			conf = new XMLConfiguration("primary.xml");
			return conf.getInt("port");
		} catch (ConfigurationException e) {
			System.err.println("primary file doesnt exist");//if primaryfile doesnt exist
		} catch (ConversionException e) {
			System.err.println("not a port");//if port doesnt exist
    	}
    	return 0;
    }
    
    private void send(Message msg) {
		try {
	    	int sendPort;
	    	InetAddress sendAddress;
			if(msg.getToPrimary()) {//send to primary
				sendAddress = mPrimaryAddress;
				sendPort = mPrimaryPort;
			} else {//send to the address
				sendAddress = msg.getClient();
				sendPort = msg.getPort();
			}
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream object_output = new ObjectOutputStream(outputStream);
			object_output.writeObject(msg);
			byte[] data = outputStream.toByteArray();
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, sendAddress, sendPort);
			mSocket2.send(sendPacket);
			//System.out.println("msg sent. port: "+sendPort);
		} catch (IOException e) {
			e.printStackTrace(); System.exit(-1);
		}
    }
    
}
