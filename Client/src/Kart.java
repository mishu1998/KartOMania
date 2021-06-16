import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.Serial;
import java.io.Serializable;

public class Kart implements Serializable, Cloneable {


    @Serial
    private static final long serialVersionUID = 7043643009834618021L;
    private transient final int rotations = 16;
    public int lapsMade = 0;
    public boolean syncRequired = true;
    public boolean hasCrashed = false;
    public boolean checkpointCrossed = false;
    public transient ImageIcon currImg;
    public int direction = 12;
    public int x, y;
    public double speed = 0;
    public Polygon hitBox;
    private int ID;
    private transient ImageIcon[] img;


    // Initialise the kart object
    public Kart(int id, int x, int y) {

        // Sets the coordinates
        this.x = x;
        this.y = y;

        img = new ImageIcon[rotations];
        ID = id;

        String path;

        // Determines the path to sprites based on the ID
        switch (id) {
            case 0 -> path = "red";
            case 1 -> path = "green";
            default -> {
                path = "";
                RunGameTest.logger.severe("Application Exception ID 302 \n Invalid ID provided");
            }
        }

        // Stores all the kart sprites required
        for (int count = 0; count < img.length; count++)
            img[count] = new ImageIcon(getClass().getResource("Sprites/" + path + count + ".png"));

        // Sets the sprite according to the direction
        currImg = img[direction];

        UpdateHitBox();
    }

    // Return the kart ID
    public int getID() {
        return ID;
    }

    // Sets the kart ID
    public void setID(int ID) {
        this.ID = ID;
    }

    // Generates the variables needed to render our kart based on the parameters at hand
    public void Generate() {

        img = new ImageIcon[rotations];

        String path;

        switch (ID) {
            case 0 -> path = "red";
            case 1 -> path = "green";
            default -> {
                path = "";
                RunGameTest.logger.severe("Application Exception ID 302 \n Invalid ID provided");
            }
        }

        for (int count = 0; count < img.length; count++)
            img[count] = new ImageIcon(getClass().getResource("Sprites/" + path + count + ".png"));

        currImg = img[direction];
        UpdateHitBox();
    }

    // Change the direction the kart is headed
    public void RotateRight() {
        if (direction < rotations - 1)
            direction++;
        else
            direction = 0;

        currImg = img[direction];
        UpdateHitBox();
        syncRequired = true;
    }

    public void RotateLeft() {
        if (direction > 0)
            direction--;
        else
            direction = 15;

        currImg = img[direction];
        UpdateHitBox();
        syncRequired = true;
    }

    // Increase the speed of the kart
    public void Accelerate() {
        if (speed < 3)
            speed += 1;
    }

    // Reduce the speed of the kart
    public void Decelerate() {
        if (speed != 0) {
            speed -= 1;
        }
    }

    // Method used to determine the displacement based on the direction and speed of the kart
    public void UpdatePosition() {

        if (!hasCrashed) {
            if (speed != 0) {

                switch (direction) {

                    case 0:
                        x += 3 * speed;
                        break;

                    case 1:
                        y += speed;
                        x += 2 * speed;
                        break;

                    case 2:
                        y += speed;
                        x += speed;
                        break;

                    case 3:
                        y += 2 * speed;
                        x += speed;
                        break;

                    case 4:
                        y += 3 * speed;
                        break;

                    case 5:
                        y += 2 * speed;
                        x += -1 * speed;
                        break;

                    case 6:
                        y += speed;
                        x += -1 * speed;
                        break;

                    case 7:
                        y += speed;
                        x += -2 * speed;
                        break;

                    case 8:
                        x += -3 * speed;
                        break;

                    case 9:
                        y += -1 * speed;
                        x += -2 * speed;
                        break;

                    case 10:
                        y += -1 * speed;
                        x += -1 * speed;
                        break;

                    case 11:
                        y += -2 * speed;
                        x += -1 * speed;
                        break;

                    case 12:
                        y += -3 * speed;
                        break;

                    case 13:
                        y += -2 * speed;
                        x += speed;
                        break;

                    case 14:
                        y += -1 * speed;
                        x += speed;
                        break;

                    case 15:
                        y += -1 * speed;
                        x += 2 * speed;
                        break;
                }
                UpdateHitBox();
            }

        } else {
            speed = 0;
        }
    }

    // Method used to rotate the hitbox of the kart based on the current direction
    private void UpdateHitBox() {

        switch (direction) {
            case 0, 8 -> hitBox = RotateHitBox(90);
            case 1, 9 -> hitBox = RotateHitBox(112.5);
            case 2, 10 -> hitBox = RotateHitBox(135);
            case 3, 11 -> hitBox = RotateHitBox(157.5);
            case 4, 12 -> hitBox = new Polygon(new int[]{x + 15, x + 35, x + 35, x + 15}, new int[]{y + 48, y + 48, y + 2, y + 2}, 4);
            case 5, 13 -> hitBox = RotateHitBox(22.5);
            case 6, 14 -> hitBox = RotateHitBox(45);
            case 7, 15 -> hitBox = RotateHitBox(67.5);
        }
    }

    // Actual modification and rotation of the hitbox
    // Credit to: Martin Frank (https://stackoverflow.com/questions/32069321/how-can-i-make-rotating-hitboxes-that-detect-collision)
    private Polygon RotateHitBox(double angle) {

        int[] xs = new int[]{x + 15, x + 35, x + 35, x + 15};
        int[] ys = new int[]{y + 48, y + 48, y + 2, y + 2};

        double anchorx = (double) (xs[0] + xs[1]) / 2;
        double anchory = (double) (ys[1] + ys[2]) / 2;

        //again: your source Points as double[]
        double[] src = new double[]{
                xs[0], ys[0],
                xs[1], ys[1],
                xs[2], ys[2],
                xs[3], ys[3]};

        //and destination points for rotating them
        double[] dst = new double[8];

        //to rotate them you need an affirm transformation
        AffineTransform t = AffineTransform.getRotateInstance(Math.toRadians(angle), anchorx, anchory);
        t.transform(src, 0, dst, 0, 4);

        //and revert them back into int[]
        int[] xTrans = new int[]{(int) dst[0], (int) dst[2], (int) dst[4], (int) dst[6]};
        int[] yTrans = new int[]{(int) dst[1], (int) dst[3], (int) dst[5], (int) dst[7]};

        //this would be your rotated polygon
        return new Polygon(xTrans, yTrans, 4);
    }

    // Method used to create a deep copy of the kart
    public Kart cloneKart() throws CloneNotSupportedException {
        return (Kart) super.clone();
    }
}