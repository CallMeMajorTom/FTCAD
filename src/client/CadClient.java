/**
 *
 * @author brom
 */

package client;

import both.GObject;
import both.Message;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CadClient {
	// globals fields
	private LinkedList<GObject> ObejectList = new LinkedList<>();
	static private GUI gui = null;
	static FEConnectionToServer m_FEConnection = null;
	private int m_Port;
	private InetAddress m_Address;
	private final int FEPort;
	private final String FE_Address;
	private int ID = 0;
	private BlockingQueue<Message> mMsgList = new LinkedBlockingQueue<Message>();

	public static void main(String[] args) throws IOException, ConfigurationException, ClassNotFoundException {
		if (args.length < 1) {
			System.err.println("Usage: not enough arguments");
			System.exit(-1);
		}
		// arguments become the address and port
		InetAddress address = InetAddress.getByName(args[0]);
		int m_port = Integer.parseInt(args[1]);
		CadClient c = new CadClient(address, m_port);
	}

	private CadClient(InetAddress m_address, int m_port)
			throws ConfigurationException, IOException, ClassNotFoundException {
		// creating CAD Client with config file with the Front End
		m_Address = m_address;
		m_Port = m_port;
		XMLConfiguration conf = new XMLConfiguration("configuration.xml");
		int i = 0;
		for (i = 0; i < 4; i++) {
			if (conf.getString("databases.database(" + i + ").role").equals("frontend"))
				break;
		}
		FEPort = conf.getInt("databases.database(" + i + ").port");
		FE_Address = conf.getString("databases.database(" + i + ").ip");
		m_FEConnection = new FEConnectionToServer(FE_Address, FEPort,mMsgList);
		m_FEConnection.start();// Keep receive message
		this.takeExpected();
	}


	private void takeExpected() throws IOException, ClassNotFoundException {
		do {
			while(m_FEConnection.mExpectedBQ.size() != 0){
				try {
					operate(m_FEConnection.mExpectedBQ.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} while (true);
	}

	public void operate(Message message) throws IOException {
		if (message.getCommand().equalsIgnoreCase("/draw")) {
			ObejectList.add(message.getObject());
		} else if (message.getCommand().equalsIgnoreCase("/remove")) {
			ObejectList.removeLast();
		}
		gui.setObjectList(ObejectList);
	}

	// getters and setters
	public InetAddress getM_Address() {
		return m_Address;
	}

	public int getM_Port() {
		return m_Port;
	}

	public FEConnectionToServer getM_FEConnection() {
		return m_FEConnection;
	}

	public int increaseID() {
		return ID++;
	}
}
