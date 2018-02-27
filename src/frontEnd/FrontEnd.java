package frontEnd;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class FrontEnd {
    private boolean mNoPrimary;
    private DatagramSocket mPrimary;
    private DatagramSocket mSocket;

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
    	mNoPrimary = true;
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
            if(!readPrimary(received)) send(received);
        } while (true);
    }
    
    private boolean readPrimary(DatagramPacket received){
    	//if msg says readprimary do it
    	//read from file and set mPrimary to it
    	//return true
    	return false;
    }
    private void send(DatagramPacket received) {
    	//send received to the address it points to
    	//or if it asks to be sent to primary send it to primary
    }
    
}
