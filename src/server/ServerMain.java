package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// main class for the server
public class ServerMain {
	int mPort = 0;

	public static void main(String[] args) {
		ServerSocket ss = null;
		Server server = null;
		if (args.length < 1) {
			System.err.println("Usage: java Server portnumber");
			System.exit(-1);
		}
		int port;
		try {
			port = Integer.parseInt(args[0]);
			ServerMain instance = new ServerMain(port);
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

	private ServerMain(int port) {
		mPort = port;
	}

	public int getPort() {
		return mPort;
	}
}
