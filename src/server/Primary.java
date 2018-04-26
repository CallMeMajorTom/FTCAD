package server;

import both.Message;

import java.io.*;
import java.net.DatagramPacket;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.XMLConfiguration;

public class Primary extends State{
	Server mServer = null;
	static boolean isPrimary = false ; 
	protected State update(Server server){
		System.out.println("Primary state");
        mServer = server;
        isPrimary = true ;
		new Thread(new ListenerThread(server.mFEConnectionToClients, server.mExpectedBQ, 
				server.mUSocket, server.mFEAddress, server.mFEPort, server.mMessageList)).start();
    	writeNtell();
    	while(true) {
    		try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace(); System.exit(-1);
			}
    		ArrayList<Message> msgs = new ArrayList<Message>(),
    			acks = new ArrayList<Message>();
			for (int i = server.mExpectedBQ.size(); 0<i ;i--) {
	    		Message msg = null;
				try {
					msg = server.mExpectedBQ.remove();
				} catch (Exception e) {
					e.printStackTrace(); System.exit(-1);//TODO Why did this happen
				}
	    		acks.add(msg);
	    		msg = new Message(msg, server.mMsgID);
	    		server.mMsgID++;
	    		server.mMessageList.add(msg);
	    		msgs.add(msg);
    		}
    		updateBackups();
    		sendAcks(acks);
    		broadcast(msgs);
    	}
    }
	public static boolean getIfPrimary()
	{
		return isPrimary ;
	}
	//writes to primary file and tells front end to read
	private void writeNtell(){
		try {
			//writes. reads from primary file and then writes to it.
			XMLConfiguration conf = new XMLConfiguration("primary.xml");
	        conf.setProperty("port",mServer.mPort);
            conf.save("primary.xml");
	        System.out.println("Port now is " + conf.getInt("port"));
			//tell FE: create a message and convert to byte array. Then send it.
			Message message = new Message(0, "/tell", null, false, null, 0);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream object_output = new ObjectOutputStream(outputStream);
			object_output.writeObject(message);
			byte[] data = outputStream.toByteArray();
			mServer.mUSocket.send(new DatagramPacket(data, data.length, mServer.mFEAddress, mServer.mFEPort));
		} catch(ConfigurationException e) {//happens if there are no primary.xml file
			e.printStackTrace();
			System.exit(-1);
        }catch (IOException e) {//TODO why happens?
			e.printStackTrace();
			System.exit(-1);
		}
	} 
	
	//TODO why does this exist?
	private int readfile() {
		XMLConfiguration conf = null;
		try {
			conf = new XMLConfiguration("server/primary.xml");
			return conf.getInt("port");
		} catch (ConfigurationException e) {
			System.err.println("primary file doesnt exist");//if primaryfile doesnt exist
		} catch (ConversionException e) {
			System.err.println("not a port");//if port doesnt exist
    	}
    	return 0;
    }

    private void updateBackups() {
		// TODO update the backups with the latest updates
	}
    
	private void sendAcks(ArrayList<Message> msgs){
        for (FEConnectionToClient cc : mServer.mFEConnectionToClients) {
        	for(Message msg : msgs) {
        		if(cc.compareClient(msg.getClient(), msg.getPort()))
        			cc.sendAck(msg);
        	}
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
