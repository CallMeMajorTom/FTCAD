package server;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class RMmessage implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String OK = "/ok";
    public static final String ELECTION = "/election";
    public static final String COORDINATOR = "/coordinator";
    public static final String PING = "/ping";
    public static final String PONG = "/pong";
    /**
     * sourceId is the origin of the message,
     * sourcePort is the port from which the message was sent
     * destinationId is the target process
     * data is a list of data items that the process likes to send
     */

    private int sourcePort;
    private int destinationPort;
    private String type;
    @SuppressWarnings("unused")
	private InetAddress sourceAddr;
    private List<Object> data = new ArrayList<Object>();

    public RMmessage(int sourcePort, String type) {
        this.sourcePort = sourcePort;
        this.type = type;
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


}