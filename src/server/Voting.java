package server;

import java.util.ListIterator;

public class Voting extends State{
    protected State update(Server server) {
    	if(0 != server.mReplicaConnections.size()){
    		//tells the other replicas election started
    		server.sendElectionMessageToPeers();
    		try {
    			Thread.sleep(50);
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    		//search the replicas for ports bigger than this replica.
    		for(ListIterator<ReplicaConnection> itr = server.mReplicaConnections.listIterator(); itr.hasNext();) {
    			int port = itr.next().mPort;
    			if (port > server.mPort) {
    				//TODO Explain what happens now?
    				synchronized (server.pendingElecResps) {
    					if (!server.pendingElecResps.containsKey(port)) {//If someone disagree with you
	                        return new backUpNI();//TODO: return backup
	                    }
	                }
	            }
	        }
	        System.out.println("P" + server.mPort + " set itself as coordinator");
	        server.Primary_Port = server.mPort;
	        for(ListIterator<ReplicaConnection> itr = server.mReplicaConnections.listIterator();itr.hasNext();){//inform everyone that you are the coordinator
	            itr.next().sendMessage(TCPMessage.COORDINATOR);//TODO:create a new message class for commnication between RM
	        }
	        return new Primary();//TODO return right type
    	} else return new Primary();
    }
}
