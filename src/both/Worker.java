package both;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Worker extends Thread{
	static double TRANSMISSION_FAILURE_RATE = 0.3;
	private DatagramPacket mMessage = null;
	private DatagramSocket mSocket = null;
	private boolean mAck;

	public Worker(DatagramPacket message, DatagramSocket l_socket, boolean acknowledgment) {
		super();
		this.mMessage = message;
		this.mSocket = l_socket;
		this.mAck = acknowledgment;
	}

	public void run() {
		do{
			try {
				mSocket.send(mMessage);
				Thread.sleep(50);
			} catch (Exception e) {
				e.printStackTrace();System.exit(-1);
			}
		} while (!mAck);
	}
}
