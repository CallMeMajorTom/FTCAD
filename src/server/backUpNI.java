package server;

public class backUpNI extends State {
	protected State update(Server server) {
		System.out.println("backUpNI state"); // Back up not integrated, state 
		while(true) {// TODO implement this right
	        if(!server.isPrimaryAliveDeadline()) return new Voting();//If the primary died now, enter the voting state
			if(server.askingForUpdate()){
				return new backUp();
			}
		}
	}
}