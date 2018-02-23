package frontEnd;

import client.ServerConnection;
import General.PingClass;
import DCAD.General.PingMessage;
import DCAD.General.PrimaryMsg;
import DCAD.General.ThreadSafeObjectStream;
import server.GetPrimaryMessage;
import server.Server;
import server.ClientConnection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class FEConnection implements Runnable{
	private Socket mClientSocket = null;
	private String mHostName = null;
    private ThreadSafeObjectStream mOut = null;
    private ObjectInputStream mIn = null;
    private int mServerPort = -1;
    private int mFEPort;
    private String mPrimaryAddress;
    private int mPrimaryPort;
    private boolean isPrimary;
    private Server mServer;

    public FEConnection(String hostName, int FEport, int serverPort, Server server) {
    	mHostName = hostName;
        mFEPort = FEport;
        mServerPort = serverPort;
        mServer = server;
    }
    
    public void clientHandshake() {
        try {
            mClientSocket = new Socket(mHostName, mFEPort);
            //mOut = new ThreadSafeObjectStream(new ObjectOutputStream(mClientSocket.getOutputStream()));
            mIn = new ObjectInputStream(mClientSocket.getInputStream());
        } catch (IOException e) {
        }
        sendClientConnectMessage();
        clientListenForFrontEndMessages();
    }

    public void serverHandshake() {
        try {
            mClientSocket = new Socket(mHostName, mFEPort);
            mOut = new ThreadSafeObjectStream(new ObjectOutputStream(mClientSocket.getOutputStream()));
            mIn = new ObjectInputStream(mClientSocket.getInputStream());
        } catch (IOException e) {}
        sendServerConnectMessage();
    }


    public void sendClientConnectMessage() {
        try {
            mOut.writeObject(new ServerConnection());    //The client doesn't have to send anything to the FE
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendServerConnectMessage() {
        try {//The FE has to know the IP, port and if it was primary
            mOut.writeObject(new ServerConnectionMessage(mHostName, mServerPort, isPrimary));    
        } catch (IOException e) {}

    }

    public void clientListenForFrontEndMessages() {
        try {
            Object object = mIn.readObject();
            if (object instanceof PingMessage) {}
            else if (object instanceof PrimaryMsg) {
                mPrimaryAddress = ((PrimaryMsg) object).getPrimaryAddress();
                mPrimaryPort = ((PrimaryMsg) object).getPrimaryPort();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void serverListenForFrontEndMessages() {
        while (true) {
            try {
                Object object = mIn.readObject();
                if (object instanceof PingMessage) {}
                else if (object instanceof PrimaryMsg) {
                    PrimaryMsg primaryMsg = (PrimaryMsg) object;
                    mPrimaryAddress = primaryMsg.getPrimaryAddress();
                    mPrimaryPort = primaryMsg.getPrimaryPort();
                    isPrimary = primaryMsg.getPrimary();
                    mServer.setPrimary(isPrimary);
                    if(isPrimary){
                        System.out.println("I am Primary");
                    }else{
                        System.out.println("I am Backup");
                    }
                }
            } catch (IOException e) {
                return;
            } catch (ClassNotFoundException e) {}
        }
    }

    public String getPrimaryAddress() {
        return mPrimaryAddress;
    }

    public int getPrimaryPort() {
        return mPrimaryPort;
    }

    public boolean getPrimary() {
        return isPrimary;
    }

    public void sendGetPrimary(){       //Sends a message to get the new primary
        try {
            mOut.writeObject(new GetPrimaryMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        while (true) {
            try {//The servers use this thread run method because they need to ping the FE
                serverHandshake();
                mClientSocket.setSoTimeout(5000);
                PingClass pingClass = new PingClass(mOut);
                Thread pingingThread = new Thread(pingClass);
                pingingThread.start();
                //When this method eventually fails it will continue down to shutdown the connection.
                serverListenForFrontEndMessages(); 
                pingClass.shutdown();
                pingingThread.interrupt();
                pingingThread.join();
                mClientSocket.close();

            } catch (IOException e) {
                System.err.println("Retrying connection to FE");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {}
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
