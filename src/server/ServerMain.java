package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

// main class for the server
public class ServerMain {

	public static void main(String[] args) {
		ServerSocket ss = null;
		Server server = null;
		if (args.length < 1) {
			System.err.println("Usage: java Server portnumber");
			System.exit(-1);
		}

		try {
			int port = Integer.parseInt(args[0]);
			server = new Server();
			// listen on specified port
			ss = new ServerSocket(port);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (true) {
			System.out.println("Server: waiting for connection ..");
			try {
				// accept connection from the client
				Socket socket = ss.accept();
				server.addClient(socket);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
