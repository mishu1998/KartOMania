import java.awt.*;
import java.io.Serial;
import java.io.Serializable;

public class Kart implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 7043643009834618021L;
    public int lapsMade = 0;
    public boolean syncRequired;
    public transient boolean syncToClient;
    public boolean hasCrashed;
    public boolean checkpointCrossed;
    public int x, y;
    public double speed;
    public Polygon hitBox;
    private int ID = -1;
    private int direction;

    // Getter for ID
    public int getID() {
        return ID;
    }

    // Setter for ID
    public void setID(int ID) {
        this.ID = ID;
    }

    // Clone our kart object
    public Kart cloneKart() throws CloneNotSupportedException {
        return (Kart) super.clone();
    }

    // Resets the position to the original position and decrements the laps made
    // ( as the player gets reset behind the finish line )
    public void resetPosition() {
        switch (ID) {
            case 0 -> x = 100;
            case 1 -> x = 170;
        }
        y = 300;

        direction = 12;

        checkpointCrossed = false;
    }
}