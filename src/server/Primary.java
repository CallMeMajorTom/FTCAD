package server;

import both.Message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;

public class Primary extends State{

    private State update(){
        return this;
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

    public synchronized void broadcast(Message message) throws IOException {
        for (FEConnectionToClient cc : m_server.mFEConnectionToClients) {
            cc.sendMessage(message);
        }
    }
}
