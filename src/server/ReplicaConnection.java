package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import both.Message;

public class ReplicaConnection extends Thread {
	private ServerSocket mTSocket;
	private Socket mSocket;
	private ObjectInputStream mIn;
	private ObjectOutputStream mOut;
	private boolean mAlive = false;
	private Server mServer;
	private String mName;

	public final int mPort;

	public ReplicaConnection(int port, Server s, ServerSocket TSocket) {
		mServer = s;
		mName = "unknown";
		mPort = port;
		mTSocket = TSocket;
		try {
			mSocket = mTSocket.accept();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}
		try {
			mOut = new ObjectOutputStream(mSocket.getOutputStream());
			mIn = new ObjectInputStream(mSocket.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		mAlive = true;
	}

	private void destructor() {
		try {
			mSocket.close();
			mOut.close();
			mIn.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		mAlive = false;
	}

	private void restart() {
		// TODO Auto-generated method stub

	}

	public void run() {
		do {
			receiveMessage();
		} while (mAlive);
	}

	public void sendMessage(String msg) {
		if (mAlive) {
			// marshalling message
			TCPMessage mmsg = new TCPMessage(msg, null, false, mPort);
			// send message to client
			try {
				mOut.writeObject(mmsg);
				mOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			System.out.println("sent: " + msg);
		}
	}

	private void receiveMessage() {
		// recieve message
		Object obj = null;
		boolean clientcrash = false;
		try {
			obj = mIn.readObject();
		} catch (SocketException e) {
			clientcrash = true;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		if (clientcrash) {
			restart();
		} else {
			// unmarshalling message
			String umsg = "";
			if (obj instanceof Message) {
				Message msg = (Message) obj;
				umsg = msg.getCommand();
			} else {
				System.err.print("Weird error");
				System.exit(-1);
			}
			System.out.println("recieved: " + umsg);
			// rpc
			mServer.controlRecieveMessage(this, umsg);
		}
	}

	public boolean hasName(String testName) {
		return testName.matches(mName);
	}

	public String getMName() {
		return new String(mName);
	}

	public void setMName(String testName) {
		mName = testName;
	}

	public boolean getAlive() {
		return mAlive;
	}

	public int receiveMessage2() {
		// TODO Auto-generated method stub
		return 0;
	}
}
