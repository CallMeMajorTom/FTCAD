package frontEnd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
			pa = InetAddress.getByName("localhost");
    	} catch (Exception e) {
    		e.printStackTrace(); 
    		System.exit(-1);
    	}
    	mPrimaryAddress = pa;
    	mPrimaryPort = readfile();//for testing only
		System.out.println(mPrimaryPort);
    }

    private void listenAndSend() {
        System.out.println("Waiting for handshake...!");
        do {
        	//receive
    		byte[] buf = new byte[256];
    		DatagramPacket received = new DatagramPacket(buf, buf.length);
            try {
            	mSocket.receive(received);
            } catch (Exception e) {
            	e.printStackTrace(); 
            	System.exit(-1);
            }
            //convert to msg
            byte[] incomingData = new byte[256];
        	DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
    		ByteArrayInputStream byte_stream = new ByteArrayInputStream(incomingPacket.getData());
    		ObjectInputStream object_stream; 
    		Message msg = null;
    		try {
    			object_stream = new ObjectInputStream(byte_stream);
    			msg = (Message)object_stream.readObject();
    		} catch (Exception e) {
    			e.printStackTrace(); 
    			System.exit(-1);
    		}
            if(!readPrimary(msg)) send(received, msg);//determine what to do with the msg
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
			System.err.println("primary file doesnt exist");System.exit(-1);//if primaryfile doesnt exist
		} catch (ConversionException e) {
			System.err.println("not a port");System.exit(-1);//if port doesnt exist
    	}
    	return 0;
    }
    
    private void send(DatagramPacket received, Message msg) {
		if(msg.getToPrimary()) {//send to primary
			if(mPrimaryPort!=-1) {
				received.setAddress(mPrimaryAddress);
				received.setPort(mPrimaryPort);
				try {
					mSocket.send(received);
				} catch (IOException e) {
					e.printStackTrace(); System.exit(-1);
				}
			}
		} else {//send to the address
			received.setAddress(msg.getClient());
			received.setPort(msg.getPort());
			try {
				mSocket.send(received);
			} catch (IOException e) {
				e.printStackTrace(); System.exit(-1);
			}
		}
    }
    
}
