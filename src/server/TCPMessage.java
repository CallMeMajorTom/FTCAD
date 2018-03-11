package server;

public class TCPMessage {

	public static final String COORDINATOR = null;
	String mmsg;
	Object mobj;
	boolean mconfirmed;
	int mport;

	public TCPMessage() {

	}

	public TCPMessage(String mmsg, Object mobj, boolean mconfirmed, int mport) {
		super();
		this.mmsg = mmsg;
		this.mobj = mobj;
		this.mconfirmed = mconfirmed;
		this.mport = mport;
	}

}
