package both;

import java.io.Serializable;
import java.net.InetAddress;
import java.time.LocalDateTime;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	private boolean msgType;//false is send, true is acknowledge
	private boolean mConfirmed = false;
	private final int mID;
	@SuppressWarnings("unused")
	private LocalDateTime mTime = LocalDateTime.now();
	private GObject mObject;
	private String mCommand;
	private boolean mToPrimary;
	private final InetAddress mClient;
	private final int mPort;

	public Message(boolean messageType, int ID,String command, GObject parameter, 
			boolean toPrimary, InetAddress lClient, int Port) {
		super();
		msgType = messageType;
		mID = ID;
		mObject = parameter;
		mCommand = command;
		mToPrimary = toPrimary;
		mClient = lClient;
		mPort = Port;
	}
	
	public Message(Message msg, int MsgID) {
		super();
		msgType = false;
		mID = MsgID;
		mObject = msg.getObject();
		mCommand = msg.getCommand();
		mToPrimary = false;
		mClient = msg.getClient();
		mPort = msg.getPort();
	}

	synchronized public boolean getConfirmed(){
		return mConfirmed;
	}
	
	synchronized public void setConfirmedAsTrue(){
		mConfirmed = true;
	}

	public boolean getMsgType(){
		return msgType;
	}
	
	public void setMsgTypeAsTrue(){
		msgType = true;
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
	
	public void setToPrimary(boolean bol) {
		mToPrimary = bol;
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
