package server;

public class backUp extends State {
	protected State update(Server server) {
		System.out.println("backUp state");
		if(!server.isPrimaryAlive()) return new Voting();//If the primary died now
		return this;//TODO implement this right
	}
}
