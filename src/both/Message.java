package both;

import java.io.Serializable;
import java.util.LinkedList;

public class Message implements Serializable {
	private LinkedList<GObject> objectList ;

	public Message(LinkedList<GObject> objectList) {
		super();
		this.objectList = objectList;
	}

	public LinkedList<GObject> getObjectList() {
		return objectList;
	}

	public void setObjectList(LinkedList<GObject> objectList) {
		this.objectList = objectList;
	}

}
