/**
 *
 * @author brom
 */

package client;

import both.Message;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class CadClient {
	static private GUI gui = null;
	static FEConnection m_FEConnection = null;

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		m_FEConnection = new FEConnection((args[0]), Integer.parseInt(args[1]));
		gui = new GUI(750, 600,m_FEConnection);
		gui.addToListener();
		CadClient c = new CadClient();
		c.listenForServerMessages();
	}

	private CadClient() {

	}

	private void listenForServerMessages() throws IOException, ClassNotFoundException {
		do {
			Message message = m_FEConnection.receiveChatMessage();
			gui.setObjectList(message.getObjectList());
		} while (true);
	}
}
