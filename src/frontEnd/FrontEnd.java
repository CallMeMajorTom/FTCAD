package frontEnd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import both.Message;

public class FrontEnd {
    private DatagramSocket mSocket;
	public InetAddress mPrimaryAddress=null;
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
    	try {
            mSocket = new DatagramSocket(portNumber);
    	} catch (Exception e) {
    		e.printStackTrace(); 
    		System.exit(-1);
    	}
    }

    private void listenAndSend() {
        System.out.println("Waiting for handshake...!");
        do {
    		byte[] buf = new byte[256];
    		DatagramPacket received = new DatagramPacket(buf, buf.length);
            try {
            	mSocket.receive(received);
            } catch (Exception e) {
            	e.printStackTrace(); 
            	System.exit(-1);
            }
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
            if(!readPrimary(msg)) send(received, msg);
        } while (true);
    }
    
    private boolean readPrimary(Message msg){
		if(msg.getCommand().equals("tell")) {
			//TODO:read from file and set address n port
			//read
			//mPrimaryAddress=;
			//mPrimaryPort=;
			return true;
		}else return false;
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
