/**
 *
 * @author brom
 */

package DCAD;

public class Cad {
    static private GUI gui = new GUI(750,600);

    public static void main(String[] args) {
        gui.addToListener();
        Cad c = new Cad();
    }
    
    private Cad() {
    }
}
