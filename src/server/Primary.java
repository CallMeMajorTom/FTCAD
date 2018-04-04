package server;

import both.Message;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.XMLConfiguration;

public class Primary extends State{
	
	Server mServer = null;

	protected State update(Server server){
		System.out.println("Primary state");
        mServer = server;
		try {
			writeNtell();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		new Thread(new ListenerThread(server.mFEConnectionToClients, server.mExpectedBQ, 
				server.mUSocket, server.mFEAddress, server.mFEPort, server.mMessageList)).start();
    	//writeNtell();
    	while(true) {
    		try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace(); System.exit(-1);
			}
    		stopProducing();
    		ArrayList<Message> msgs = new ArrayList<Message>();
    		for (int i = server.mExpectedBQ.size(); 0<i ;i--) {
	    		Message msg = null;
				try {
					msg = server.mExpectedBQ.take();
				} catch (InterruptedException e) {
					e.printStackTrace(); System.exit(-1);//TODO Why did this happen
				}
	    		msg = new Message(msg, server.mMsgID);
	    		server.mMsgID++;
	    		server.mMessageList.add(msg);
	    		msgs.add(msg);
    		}
    		updateBackups();
    		sendAcks();
    		startProducing();
    		broadcast(msgs);
    	}
    }
	
	private void writeNtell() throws ConfigurationException {
		XMLConfiguration conf = new XMLConfiguration("primary.xml");
		conf.setProperty("port",mServer.mPort);
		//TODO tell FE
	} 
	
	private int readfile() {
		XMLConfiguration conf = null;
		try {
			conf = new XMLConfiguration("primary.xml");
			return conf.getInt("port");
		} catch (ConfigurationException e) {
			System.err.println("primary file doesnt exist");//if primaryfile doesnt exist
		} catch (ConversionException e) {
			System.err.println("not a port");//if port doesnt exist
    	}
    	return 0;
    }

	private void stopProducing() {
    	//TODO stop producing
	}

	private void startProducing() {
		//TODO start producing
	}

    private void updateBackups() {
		// TODO update the backups with the latest updates
	}
    
	private void sendAcks(){
        for (FEConnectionToClient cc : mServer.mFEConnectionToClients) {
            cc.sendAcks();
        }
    }
    
    private void broadcast(ArrayList<Message> msgs){
    	for (Message msg : msgs) {
	        for (FEConnectionToClient cc : mServer.mFEConnectionToClients) {
	            cc.sendMessage(msg);
	        }
	        System.out.println("broadcast. number of clients: "+mServer.mFEConnectionToClients.size());
    	}
    }
}
