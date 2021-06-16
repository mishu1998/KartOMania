import javax.swing.*;
import java.awt.*;

class MenuWindow extends JFrame {

    public static final int WIDTH = 500;  //pixels
    public static final int HEIGHT = 300; //pixels
    public Container cp = getContentPane();

    // Generation of the menu window
    public MenuWindow() {
        super();

        // Setting the title and boundary of our window
        setTitle("Main Menu");
        setBounds(250, 250, WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Adding our panel to the window
        MenuPanel mp = new MenuPanel();
        cp.add(mp);

        setVisible(true);
    }
}
