package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import both.Message;

public class ReplicaConnection extends Thread {
	public final int mPort;
	
	private ServerSocket mTSocket;
	private Socket mSocket;
	private ObjectInputStream mIn;
	private ObjectOutputStream mOut;
	private boolean mAlive = false;
	private Server mServer;
	private String mName;

	public ReplicaConnection(int port, Server s, ServerSocket TSocket) {
		mServer = s;
		mName = "unknown"+port;
		mPort = port;
		mTSocket = TSocket;
	}

	public void run() {
        //starting connection
        while (true) {
            while (startConnection()) { //Keep receive msg when the connection is established successfully
                receiveMessage();
            }
        }
    }

	public void sendMessage(String msg) throws Exception {
		if (mAlive) {
			// marshalling message
			TCPMessage mmsg = new TCPMessage(msg, null, false, mPort);
			// send message to other RMs
			try {
				mOut.writeObject(mmsg);
				mOut.flush();
			} catch (IOException e) {
				System.err.println("The RM you try to send msg is not alive");
				mAlive = false;
			}
			System.out.println("sent: " + msg);
		} else throw new Exception();
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

	private void receiveMessage() {
		// recieve message
		Object obj = null;
		try {
			obj = mIn.readObject();
		} catch (SocketException e) {
			mAlive = false;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		if (mAlive) {
			startConnection();
		} else {
			// unmarshalling message
			String umsg = "";
			if (obj instanceof Message) {
				Message msg = (Message) obj;
				umsg = msg.getCommand();
			} else {
				System.err.print("ReplicaConnection error");
				System.exit(-1);
			}
			System.out.println("recieved: " + umsg);
			// rpc
			RMmessage crm = new RMmessage(mPort, "");
			mServer.controlRecieveMessage(this, crm);
		}
	}
	
	public void destructor() {
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

	private boolean startConnection() {
		try {
			mSocket = mTSocket.accept();
		} catch (IOException e1) {
		    System.err.println("The server you try to connect is not alive");
		    return false;
		}
		try {
			mOut = new ObjectOutputStream(mSocket.getOutputStream());
			mIn = new ObjectInputStream(mSocket.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		mAlive = true;
		return true;
	}
}
