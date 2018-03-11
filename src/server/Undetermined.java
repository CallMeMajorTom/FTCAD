package server;

import java.util.HashMap;
import java.util.Map;

public class Undetermined extends State {

    protected State update() {
        Voting voting = new Voting();
        if(m_server.mReplicaConnections.size() == 0) return voting;//If the RMList is empty, thats mean only this server exits
        else if(!m_server.isPrimaryAlive()) return voting;//If the primary died now
        else return null;//TODO:return backup
    }
}
