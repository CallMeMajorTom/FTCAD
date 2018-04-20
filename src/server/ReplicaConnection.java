package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;

import both.Message;

public class ReplicaConnection extends Thread {
	public final int mPort;
	private ServerSocket mTSocket;
	private Socket mSocket;
	private ObjectInputStream mIn;//used for in going communication
	private ObjectOutputStream mOut;//used for out going communication
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

	public void sendMessage(RMmessage msg) throws Exception {
		if (mAlive) {
			try {
				mOut.writeObject(msg);
				mOut.flush();
			} catch (IOException e) {
				System.err.println("The RM you try to send msg is not alive");
				mAlive = false;
			}

		} else throw new Exception();
	}

	public void sendList(ArrayList<Message> m){
		try {
			mOut.writeObject(m);
		} catch (IOException e) {
			e.printStackTrace();
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

	private void receiveMessage() {
		// recieve message
		Object obj = null;
		try {
			obj = mIn.readObject();
		} catch (SocketException e) {
		    System.err.println("ReplicaConnection: "+mPort+" is dead");
			mAlive = false;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		if (!mAlive) {
			startConnection();
		} else {
		    //System.out.println(obj.getClass());
			// unmarshalling message
			if (obj instanceof RMmessage) {
				RMmessage msg = (RMmessage) obj;
				mServer.controlRecieveMessage(msg);
			} else if(obj instanceof ArrayList<?>) {
				ArrayList<Message> messageList = (ArrayList<Message>) obj;
				if(!mServer.mMessageList.equals(messageList)){
					System.out.println("Update version:"+ mServer.Update_Version());
					mServer.mMessageList = messageList;
				}
			}else {
                System.err.print("ReplicaConnection error");
                System.exit(-1);
            }
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
		if(mPort < mServer.mPort) {//initiate the accept
			try{
				mSocket = new Socket(mTSocket.getInetAddress(),mPort);//socket
			}catch(ConnectException e) {// cant connect
				return false;
			} catch (IOException e) {e.printStackTrace(); System.exit(-1);}//TODO why happen?
		} else if(mPort > mServer.mPort){//wait for accept
			try {
				mSocket = mTSocket.accept();
			} catch (IOException e1) {
			    System.err.println("The server you try to connect is not alive");
			    return false;
			}
		} else { 
		    System.err.println("rc: not right port");
		    System.exit(-1);
		}
		try {
			mOut = new ObjectOutputStream(mSocket.getOutputStream());
			mIn = new ObjectInputStream(mSocket.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	    System.out.println("ReplicaConnection: "+mPort+" is alive");
		mAlive = true;
		return true;
	}
}
