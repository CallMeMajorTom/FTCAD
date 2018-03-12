package server;

import both.Message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.util.ArrayList;

public class Primary extends State{
	
	Server mServer = null;

    protected State update(Server server){
    	mServer = server;
		//write to primary file
		//tell frontend to read
    	while(true) {
    		//updatedelay
    		//stop producing
    		ArrayList<Message> msgs;
    		for (int i = server.mExpectedBQ.size(); 0<i ;i--) {
	    		Message msg = new server.mExpectedBQ.take();
	    		msg = Message(msg, server.mID);
	    		server.mID++;
	    		server.mMessageList.add(msg);
	    		msgs.add(msg);
    		}
    		//update backups
    		sendAcks();
    		//start producing
    		broadcast(msgs);//change to send each message in msgs to every client
    	}
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
