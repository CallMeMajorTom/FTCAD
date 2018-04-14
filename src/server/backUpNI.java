package server;

public class backUpNI extends State {
	protected State update(Server server) {
		System.out.println("backUpNI state");
		if(!server.isPrimaryAlive()) return new Voting();//If the primary died now, enter the voting state
		if(server.askingForUpdate()){
			return new backUp();
		}
		return this;// TODO implement this right
	}
}
