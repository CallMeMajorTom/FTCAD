package server;

import java.util.ListIterator;

public class Voting extends State{
	static int noServers;
	static int coordinator ;
	static int[] status;
	static int[] servers ;
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

	        System.out.println("P " + server.mPort + " set itself as coordinator");
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
    public void startBully(){
    	// bully algorithm
        noServers = Server.getReplicaConnections() ; ;
    	
    	System.out.println("current server will initiate election") ;
    	int electi = 0;
    	electi = getCurrentServerReplica() ;
    	elect(electi) ;
        }
        static void elect(int electi)
    	{
    		// clarity for output
    		electi = electi - 1 ;
    		coordinator = electi + 1 ;
    		// goes through each server and compares to each other
    		for(int i = 0 ; i < noServers ; i ++)
    		{
    			if(servers[electi] < servers[i]){}
    			
    				System.out.println("Election message is sent from " + (electi + 1) + " process " + (i + 1)) ;
    				if( status[i] == 1){
    					elect(i + 1) ;
    				}
    			}
    		}
}
