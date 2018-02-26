package frontEnd;

public class FrontEnd {
    private boolean mNoPrimary;
    //private ? mPrimary;

    public static void main(String[] args) {//starts constructor and do a handshake
        if (args.length < 1) {
            System.err.println("Usage: java FrontEnd portnumber");
            System.exit(-1);
        }
        try {
            FrontEnd instance = new FrontEnd(Integer.parseInt(args[0]));
            instance.listenAndSend();
        } catch (NumberFormatException e) {
            System.err.println("Error: port number must be an integer.");
            System.exit(-1);
        }
    }
    private FrontEnd(int portNumber) {//initialize variables
            mNoPrimary = true;
            //socket = f(portNumber);
    }

    private void listenAndSend() {
        System.out.println("Waiting for handshake...!");
        do {
            //? recieved = socket.recieve();
            //if(recieved.tell()) readPrimary();
            //else mPrimary.send(recieved);
        } while (true);
    }
    
    private void readPrimary(){ 
    	//read from file and set mPrimary to it
    }
}
