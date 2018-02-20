/**
 *
 * @author brom
 */

package DCAD;

final class Shape {
    private String type;

    private Shape(String type) {
	this.type = type;
    }

    @Override
    public String toString() {return type;}

    public static Shape OVAL  = new Shape("OVAL");
    public static Shape RECTANGLE = new Shape("RECTANGLE");
    public static Shape LINE = new Shape("LINE");
    public static Shape FILLED_RECTANGLE = new Shape("FILLED_RECTANGLE");
    public static Shape FILLED_OVAL = new Shape("FILLED_OVAL");
}
