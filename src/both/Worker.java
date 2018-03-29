package both;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
//handles the sending of server and client messages
public class Worker extends Thread{
	static double TRANSMISSION_FAILURE_RATE = 0.3;
	private final int mDiffusion = 1;
	private DatagramPacket mMessage = null;
	private DatagramSocket mSocket = null;
	private Message mAck;

	public Worker(DatagramPacket message, DatagramSocket l_socket, Message acknowledgment) {
		super();
		this.mMessage = message;
		this.mSocket = l_socket;
		this.mAck = acknowledgment;
	}

	public void run() {
		do{
			try {
				for(int i = 0; i<mDiffusion;i++) 
					mSocket.send(mMessage);
				System.out.println("message sent: "+mAck.getCommand()+" "+mMessage.getPort());
				Thread.sleep(250);
			} catch (Exception e) {
				e.printStackTrace();System.exit(-1);
			}
		} while (!mAck.getConfirmed());
		System.out.println("ack done");
	}
}
