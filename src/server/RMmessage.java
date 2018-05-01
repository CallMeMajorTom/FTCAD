package server;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class RMmessage implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String OK = "/ok";//acknowledge someone else of being primary
    public static final String ELECTION = "/election";//start of voting
    public static final String COORDINATOR = "/coordinator";//declare oneself as primary
    public static final String PING = "/ping";//asking if alive
    public static final String PONG = "/pong";//answering if alive
    public static final String UPDATE = "/update";
    
    //sourceId is the origin of the message. sourcePort is sourceId
    //sourcePort is the port from which the message was sent
    private int sourcePort;
    private int destinationPort;
    private List<Object> data = new ArrayList<Object>();

    private String type;
	private InetAddress sourceAddr;
	private boolean mReply;
	public Object obj;

    public RMmessage(int sourcePort, int destinationPort, String type, boolean reply) {
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.type = type;
        mReply = reply;
    }

    public List<Object> getData() {
        return data;
    }

    public int getSourcePort() {
    	return sourcePort;
    }
    
    public void setSourcePort(int sourcePort) {
    	this.sourcePort = sourcePort;
    }
    
    public int getDestinationPort() {
        return destinationPort;
    }
    
    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

	public boolean isReply() {
		// TODO Auto-generated method stub
		return mReply;
	}
}