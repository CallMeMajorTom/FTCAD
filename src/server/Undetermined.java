package server;


import org.apache.commons.configuration.ConfigurationException;

public class Undetermined extends State {

    protected State update(Server server){
    	System.out.println("Undetermined state");
        if(Server.mReplicaConnections.size() == 0) 
        	return new Voting();//If the RMList is empty, thats mean only this server exists
        else if(server.isPrimaryAliveDeadline())//ask if someone is primary
        	return new backUpNI();//become a backupni if someone is primary
        else return new Voting();
    }
}
