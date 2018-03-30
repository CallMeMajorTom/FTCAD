package server;

public class Undetermined extends State {

    protected State update(Server server) {
    	System.out.println("Undetermined state");
        Voting voting = new Voting();
        if(server.mReplicaConnections.size() == 0) return voting;//If the RMList is empty, thats mean only this server exits
        else if(!server.isPrimaryAlive()) return voting;//If the primary died now
        else return null;//TODO:return backup
    }
}
