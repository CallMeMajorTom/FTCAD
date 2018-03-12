package server;

import both.Message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.XMLConfiguration;

public class Primary extends State{
	
	Server mServer = null;

    protected State update(Server server){
    	mServer = server;
    	writeNtell();
    	while(true) {
    		Thread.sleep(50);
    		stopProducing();
    		ArrayList<Message> msgs;
    		for (int i = server.mExpectedBQ.size(); 0<i ;i--) {
	    		Message msg = server.mExpectedBQ.take();
	    		msg = new Message(msg, server.MsgID);
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
    
	private void writeNtell() {
		//TODO write to primary file and tell frontend to read
		@SuppressWarnings("unused")
		XMLConfiguration conf = null;
		try {
			conf = new XMLConfiguration("primary.xml");
		} catch (ConfigurationException e) {
			//TODO create primary file
		}
		//TODO change port to this servers port with help  of conf
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
    	}
    }
}
