/**
 *
 * @author brom
 */

package client;

import both.GObject;
import both.Message;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import server.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;

public class CadClient {

    private LinkedList<GObject> ObejectList = new LinkedList<>();
	static private GUI gui = null;
	static FEConnection m_FEConnection = null;
	private int m_Port;
	private InetAddress m_Address;
    private final int FEPort;
    private final String FE_Address;

	public static void main(String[] args) throws IOException, ConfigurationException {
        if (args.length < 1) {
            System.err.println("Usage: not enough arguments");
            System.exit(-1);
        }
        InetAddress address = InetAddress.getByName(args[0]);
        int m_port = Integer.parseInt(args[1]);
        CadClient c = new CadClient(address,m_port);
	}

	private CadClient(InetAddress m_address, int m_port) throws ConfigurationException, SocketException, UnknownHostException {
        m_Address = m_address;
        m_Port = m_port;
        XMLConfiguration conf = new XMLConfiguration("configuration.xml");
        int i = 0;
        for(i = 0;i < 4; i++){
            if(conf.getString("databases.database("+i+").role").equals("frontend"))
                break;
        }
        FEPort = conf.getInt("databases.database("+i+").port");
        FE_Address = conf.getString("databases.database("+i+").ip");
        m_FEConnection = new FEConnection(FE_Address,FEPort);
	}

	private void listenForServerMessages() throws IOException, ClassNotFoundException {
		do {
			Message message = m_FEConnection.receiveChatMessage();
			if(message.getCommand().equalsIgnoreCase("/draw")){
			    ObejectList.add(message.getObject());
            }
            else if(message.getCommand().equalsIgnoreCase("/remove")){
			    ObejectList.removeLast();
            }
			gui.setObjectList(ObejectList);
		} while (true);
	}

	public InetAddress getM_Address(){
        return m_Address;
    }

    public int getM_Port(){
        return m_Port;
    }

    public FEConnection getM_FEConnection(){
	    return m_FEConnection;
    }
}
