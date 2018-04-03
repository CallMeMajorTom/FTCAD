package server;

import java.util.ListIterator;

public class Voting extends State{
    protected State update(Server server) {
    	System.out.println("Voting state");
    	if(0 != server.mReplicaConnections.size()){
    		//Start the election and tell every one
    		server.sendElectionMessageToPeers();
    		try {
    			Thread.sleep(50);//wait a minute to get all the respondes
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}

    		for(ListIterator<ReplicaConnection> itr = server.mReplicaConnections.listIterator(); itr.hasNext();) {
    			ReplicaConnection rmc = itr.next();
    			if (rmc.mPort > server.mPort && rmc.getAlive()) { //Check the alive server who has larger port number
    				synchronized (server.pendingElecResps) {
    					if (!server.pendingElecResps.containsKey(rmc.mPort)) {
    						//If the server is not in the pendingElectionRespondes list, that's mean he disagree with this election, thats mean you cant be the primary
	                        return new backUpNI();
	                    }
	                }
	            }
	        }

	        System.out.println("P" + server.mPort + " set itself as coordinator");
	        server.Primary_Port = server.mPort;
	        for(ListIterator<ReplicaConnection> itr = server.mReplicaConnections.listIterator();itr.hasNext();) {//inform everyone that you are the coordinator
				ReplicaConnection rmc = itr.next();
				if (rmc.getAlive()) {
					try {
						itr.next().sendMessage(RMmessage.COORDINATOR);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
	        return new Primary();
    	}
    	else return new Primary();
    }
}
