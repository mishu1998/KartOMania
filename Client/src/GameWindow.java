import javax.swing.*;
import java.awt.*;

class GameWindow extends JFrame {

    public static final int WIDTH = 1280;  //pixels
    public static final int HEIGHT = 720; //pixels
    public static KartAnimationPanel kartPanel;
    public Container pane = getContentPane();

    public GameWindow() {

        // Initialises the game windows and set the bounds, name and background
        super();
        setBounds(100, 100, WIDTH, HEIGHT);
        setTitle("Client " + MenuPanel.clientID);
        pane.setBackground(Color.WHITE);

        // Assigns the panel to the Game Window and attaches a key listener
        kartPanel = new KartAnimationPanel();
        pane.add(kartPanel);
        addKeyListener(kartPanel);

        // ensure application close when window closed
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //set window visible
        setVisible(true);

        kartPanel.startAnimation();

    }

}