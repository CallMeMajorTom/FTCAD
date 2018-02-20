/**
 *
 * @author brom
 */

package client;

import both.Message;

public class CadClient {
	static private GUI gui = null;
	static ServerConnection sc = null;

	public static void main(String[] args) {
		sc = new ServerConnection((args[0]), Integer.parseInt(args[1]));
		sc.connect() ;
		gui = new GUI(750, 600, sc);
		gui.addToListener();
		CadClient c = new CadClient();
		c.listenForServerMessages();
	}

	private CadClient() {

	}

	private void listenForServerMessages() {
		do {
			Message message = sc.receiveMessage();
			gui.setObjectList(message.getObjectList());

		} while (true);
	}
}
