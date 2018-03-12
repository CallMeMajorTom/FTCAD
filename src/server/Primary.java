package server;

import both.Message;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.XMLConfiguration;

public class Primary extends State{
	
	Server mServer = null;

    @SuppressWarnings("null")
	protected State update(Server server){
    	mServer = server;
    	writeNtell();
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
	
	@SuppressWarnings("unused")
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
