import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public class GameHandler {

    // Variables used to store the location of all the objects used to draw the arena
    private final Rectangle finishLine = new Rectangle(80, 280, 155, 10);
    private final Rectangle checkpoint = new Rectangle(1042, 320, 155,10);
    private final Rectangle centralBorder1 = new Rectangle(239, 239, 81, 161);
    private final Rectangle centralBorder2 = new Rectangle(959, 239, 81, 241);
    private final Line2D[] borderLines = {
            new Line2D.Float(80, 80, 480, 80),
            new Line2D.Float(480, 80, 480, 240),
            new Line2D.Float(480, 240, 800, 240),
            new Line2D.Float(800, 240, 800, 80),
            new Line2D.Float(800, 80, 1200, 80),
            new Line2D.Float(1200, 80, 1200, 640),
            new Line2D.Float(1200, 640, 800, 640),
            new Line2D.Float(800, 640, 800, 560),
            new Line2D.Float(800, 560, 80, 560),
            new Line2D.Float(80, 560, 80, 80),
            new Line2D.Float(320, 400, 960, 400)
    };

    // List designed to store all previous kart states
    public List<Kart> previousKartStates = new ArrayList<>();

    // Method used to check collision of a kart
    public synchronized Kart check(Kart kart) {

        // Preserves the last 10 kart states
        if (previousKartStates.toArray().length >= 10) {
            previousKartStates.remove(0);
        }
        previousKartStates.add(kart);

        // Checks if the karts have crashed in each other
        if(ServerListener.clientsKart.size() == 2)
            if(kart.hitBox.intersects(ServerListener.clientsKart.get(0).hitBox.getBounds()) && kart.hitBox.intersects(ServerListener.clientsKart.get(1).hitBox.getBounds())) {
                TCPserver.logger.info("Karts have crashed in each other at " + kart.x + " | " + kart.y);
                kart.hasCrashed = true;
                kart.speed = 0;
                kart.syncRequired = kart.syncToClient = true;
                return kart;
            }

        // Verifies if the kart has collided and acts accordingly
        if (collisionCheck(kart)) {
            // If the kart had a speed less of 2, it will attempt to restore the first position prior to the collision
            if (kart.speed < 2) {
                try {
                    for (Kart kartState : previousKartStates) {
                        if (!collisionCheck(kartState)) {
                            kart = kartState.cloneKart();
                            break;
                        }
                    }
                } catch (CloneNotSupportedException e) {
                    TCPserver.logger.severe("Unexpected Exception ID 902 \n" + e.getMessage() );
                }
                // If the kart has a speed of 2 or greater, the position will be reset to the start/ finish line
            } else {
                kart.resetPosition();
            }

            // Sets the kart speed to 0 and indicates that an updated kart has to be sent to both clients
            kart.speed = 0;
            kart.syncRequired = kart.syncToClient = true;
        }

        //Verifies if a lap has been made
        if (kart.hitBox.getBounds().intersects(checkpoint) && !kart.checkpointCrossed) {
            kart.checkpointCrossed = true;
            kart.syncToClient = true;
        }

        if (kart.hitBox.getBounds().intersects(finishLine) && kart.checkpointCrossed ) {
            kart.checkpointCrossed = false;
            kart.lapsMade++;
            kart.syncToClient = true;
        }


        return kart;

    }


    // Method used to check for collisions
    private boolean collisionCheck(Kart kart) {

        // Disassembles our hitbox polygon
        int[] xs = kart.hitBox.xpoints;
        int[] ys = kart.hitBox.ypoints;

        // Creates a list of lines which show the 4 sides of the kart
        Line2D[] lines = {
                new Line2D.Float(xs[0], ys[0], xs[1], ys[1]), // back side
                new Line2D.Float(xs[1], ys[1], xs[2], ys[2]), // right side
                new Line2D.Float(xs[2], ys[2], xs[3], ys[3]), // front side
                new Line2D.Float(xs[3], ys[3], xs[0], ys[0]), // left side
        };


        // If the kart intersects the borders, our method will return true, denoting that a collision has happened
        // We iterate trough each line and rectangle in order to determine this
        for (Line2D l : lines) {
            if (centralBorder1.intersectsLine(l) || centralBorder2.intersectsLine(l)) {
                return true;
            }
            for (Line2D line : borderLines) {
                if (line.intersectsLine(l)) {
                    return true;
                }
            }
        }
        return false;
    }

}
