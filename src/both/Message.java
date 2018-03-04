package both;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.LinkedList;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	private GObject mObject;
	private String mCommand;
	private	final boolean mToPrimary;
	private final InetAddress mClient;
	private final int mPort;

	public Message(String command,GObject parameter, boolean toPrimary, InetAddress lClient, int Port) {
		super();
		mObject = parameter;
		mCommand = command;
		mToPrimary = toPrimary;
		mClient = lClient;
		mPort = Port;
	}

	/*public LinkedList<GObject> getObjectList() {
		return objectList;
	}

	public void setObjectList(LinkedList<GObject> objectList) {
		this.objectList = objectList;
	}*/

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
	
	public int getPort() {
		return mPort;
	}
}
