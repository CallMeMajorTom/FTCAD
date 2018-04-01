package server;

public class Undetermined extends State {

    protected State update(Server server) {
    	System.out.println("Undetermined state");
        Voting voting = new Voting();
        if(server.mReplicaConnections.size() == 0) 
        	return voting;//If the RMList is empty, thats mean only this server exists
        else if(server.isPrimaryAlive())//ask if someone is primary
        	return new backUpNI();//become a backupni
        else return voting;
    }
}
