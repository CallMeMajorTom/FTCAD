package both;

import java.io.Serializable;
import java.net.InetAddress;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	private boolean msgType;
	private final int mID;
	private GObject mObject;
	private String mCommand;
	private	final boolean mToPrimary;
	private final InetAddress mClient;
	private final int mPort;
	private boolean confirmed = false;


	public Message(boolean messageType,int ID,String command,GObject parameter, boolean toPrimary, InetAddress lClient, int Port) {
		super();
		msgType = messageType;
		mID = ID;
		mObject = parameter;
		mCommand = command;
		mToPrimary = toPrimary;
		mClient = lClient;
		mPort = Port;
	}

	public boolean getConfirmed(){
		return confirmed;
	}

	public boolean getMsgType(){
		return msgType;
	}

	public GObject getObject() {
		return mObject;
	}

	public String getCommand() {
		return mCommand;
	}
	
	public boolean getToPrimary() {
		return mToPrimary;
	}
	
	public InetAddress getClient() {
		return mClient;
	}

	public int getID(){
		return mID;
	}
	
	public int getPort() {
		return mPort;
	}
}
