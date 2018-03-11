package server;

import both.Message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;

public class Primary extends State{
	
	Server mServer = null;

    protected State update(Server server){
    	mServer = server;
		//write to primary file
		//tell frontend to read
    	while(true) {
    		//TODO take from blockingqueue.
    		//TODO make a new message from it with a id
    		//save message to list
    		//TODO send message to every client
    	}
    }

    private void listenForClientMessages() {
        System.out.println("Waiting for client messages... ");
        do {
            byte[] incomingData = new byte[256];
            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            ByteArrayInputStream byte_stream =new ByteArrayInputStream(incomingPacket.getData());
            try {
                ObjectInputStream object_stream = new ObjectInputStream(byte_stream);
                Message message = (Message)object_stream.readObject();
            } catch (Exception e) {e.printStackTrace(); System.exit(-1);}
        }
        while(true);
    }

    private synchronized void broadcast(Message message) throws IOException {
        for (FEConnectionToClient cc : mServer.mFEConnectionToClients) {
            cc.sendMessage(message);
        }
    }
}
