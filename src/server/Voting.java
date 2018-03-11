package server;

import both.Message;

import java.util.ListIterator;

public class Voting extends State{

    protected State update() {
        m_server.holdingElection = true;
        m_server.sendElectionMessageToPeers();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.out.println(sockets);
        for(ListIterator<ReplicaConnection> itr = m_server.mReplicaConnections.listIterator(); itr.hasNext();) {
            int port = itr.next().mPort;
            if (port > m_server.mPort) {
                synchronized (m_server.pendingElecResps) {
                    if (!m_server.pendingElecResps.containsKey(port)) {//If someone disagree with you
                        return null;//TODO: return backup
                    }
                }
            }
        }
        System.out.println("P" + m_server.mPort + " set itself as coordinator");
        m_server.Primary_Port = m_server.mPort;
        for(ListIterator<ReplicaConnection> itr = m_server.mReplicaConnections.listIterator();itr.hasNext();){//inform everyone that you are the coordinator
            itr.next().sendMessage(Message.COORDINATOR);//TODO:create a new message class for commnication between RM
        }
        m_server.holdingElection = false;
        Primary primary = new Primary();
        return primary;
    }
}
