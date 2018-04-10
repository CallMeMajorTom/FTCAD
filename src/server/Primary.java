package server;

import both.Message;

import java.io.*;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.XMLConfiguration;

public class Primary extends State{
	Server mServer = null;

	protected State update(Server server){
		System.out.println("Primary state");
        mServer = server;
		new Thread(new ListenerThread(server.mFEConnectionToClients, server.mExpectedBQ, 
				server.mUSocket, server.mFEAddress, server.mFEPort, server.mMessageList)).start();
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
					msg = server.mExpectedBQ.remove();
				} catch (Exception e) {
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

	//writes to primary file and tells frontend to read
	private void writeNtell(){
		//writes
		XMLConfiguration conf = null;
		try {
			conf = new XMLConfiguration("primary.xml");
		}catch(ConfigurationException e) {//TODO what are the conditions for this exception?
			e.printStackTrace();
			System.exit(-1);
		}
        /*Properties properties = new Properties();
		properties.setProperty("port", String.valueOf(mServer.mPort));
        try {
            properties.storeToXML(new FileOutputStream("primary.xml"), "");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        conf.setProperty("port",mServer.mPort);
        try {
            conf.save("primary.xml");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        System.out.println("Port now is " + conf.getInt("port"));
		//tell FE
		Message message = new Message(0, "/tell", null, false, null, 0);
			// convert message to bytearray
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream object_output = new ObjectOutputStream(outputStream);
			object_output.writeObject(message);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		byte[] data = outputStream.toByteArray();
			// send
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, mServer.mFEAddress, mServer.mFEPort);
		try {
			mServer.mUSocket.send(sendPacket);
		} catch (IOException e) {
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
