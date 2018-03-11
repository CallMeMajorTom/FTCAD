package server;

public abstract class State {
    abstract State update(Server server);
}
