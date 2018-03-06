package server;

public class TCPMessage {

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
