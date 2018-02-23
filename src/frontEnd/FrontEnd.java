package frontEnd;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class FrontEnd {
	private ServerSocket mFESocket;
	private Socket mClientSocket;
    private boolean noPrimary;
    private FEClientConnection mPrimaryServer;
    private long mTime;
    private Vector<FEClientConnection> mConnectedServers;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java FrontEnd portnumber");
            System.exit(-1);
        }
        try {
            FrontEnd instance = new FrontEnd(Integer.parseInt(args[0]));
            instance.listenForClientHandshake();
        } catch (NumberFormatException e) {
            System.err.println("Error: port number must be an integer.");
            System.exit(-1);
        }
    }
    private FrontEnd(int portNumber) {
        try {
            mConnectedServers = new Vector<>();
            noPrimary = true;
            mFESocket = new ServerSocket(portNumber);
            mTime = System.currentTimeMillis();
        } catch (IOException e) {
            System.err.println("Could not bind Front End-Socket: " + e.getMessage());
        }
    }

    public void waitForConnections() {
        if (mTime + 3000 > System.currentTimeMillis()) {//Awaits all servers to connect before choosing the primary
            try {
                Thread.sleep(mTime + 3000 - System.currentTimeMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void listenForClientHandshake() {//Client in this case is both "normal" clients and servers
        System.out.println("Waiting for handshake...!");
        do {
            FEClientConnection feClientConnection = null;
            try {
                mClientSocket = mFESocket.accept();
                feClientConnection = new FEClientConnection(mClientSocket, this);

                Thread feClientThread = new Thread(feClientConnection);
                feClientThread.start();

            } catch (IOException e) {
                System.err.println("Error while accepting packet: " + e.getMessage());
            }
        } while (true);
    }

    public synchronized void addServer(FEClientConnection feClientConnection, boolean wasPrimary) {
        mConnectedServers.add(feClientConnection);
        if (mPrimaryServer == null && wasPrimary) {
        	//If there is no primary server, the current connection will be the new primary
            mPrimaryServer = feClientConnection;
            noPrimary = false;
        }
    }

    public void setNoPrimary(boolean value) {
        noPrimary = value;
    }

    public synchronized void removeServer(FEClientConnection feClientConnection) {
        if (feClientConnection == mPrimaryServer) {//If primary crashes, chooses the next one in the list
            setNoPrimary(true);
            System.out.println("Primary has crashed!");
            mConnectedServers.remove(feClientConnection);
            if(mConnectedServers.size() == 0){
                mPrimaryServer = null;
            }
            else{
                mPrimaryServer = mConnectedServers.firstElement();
            }
        }
        else{
            mConnectedServers.remove(feClientConnection);
        }

    }

    public FEClientConnection getPrimaryServer() {
        if (noPrimary) {        //If we don't have a primary server yet
            mPrimaryServer = mConnectedServers.firstElement();
            //mPrimaryServer.setPrimary(true);
        }
        return mPrimaryServer;
    }
    
}
