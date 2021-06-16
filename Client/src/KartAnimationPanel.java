import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Line2D;

class KartAnimationPanel extends JPanel implements KeyListener, ItemListener {

    public static int gameState = -1;
    private static Kart redKart = new Kart(0, 100, 300);
    private static Kart greenKart = new Kart(1, 170, 300);
    private final JLabel redKartImg = new JLabel(redKart.currImg);
    private final JLabel greenKartImg = new JLabel(greenKart.currImg);
    private final Rectangle finishLine = new Rectangle(80, 280, 155, 10);
    private final Rectangle centralBorder1 = new Rectangle(239, 239, 81, 161);
    private final Rectangle centralBorder2 = new Rectangle(959, 239, 81, 241);

    // List of all the borderlines used to draw our arena
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

    private final JToggleButton readyToggleButton = new JToggleButton();
    private final JLabel messageLabel = new JLabel();

    private Timer kartAnimation;
    private boolean running = false;
    private boolean preparing = false;


    // Getter and Setter for the kart objects
    public static Kart getRedKart() {
        return redKart;
    }
    public static void setRedKart(Kart redKart) {
        KartAnimationPanel.redKart = redKart;
    }
    public static Kart getGreenKart() {
        return greenKart;
    }
    public static void setGreenKart(Kart greenKart) {
        KartAnimationPanel.greenKart = greenKart;
    }

    // Paints to screen our components
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setLayout(null);

        add(readyToggleButton);
        add(messageLabel);

        Render(g);
    }

    // Method used to paint to screen our game board
    public void Render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Different colours used to draw our track
        Color c1 = Color.black;
        Color c2 = Color.blue;
        Color c3 = Color.yellow;
        Color c4 = Color.red;

        // Different strokes used to draw our track
        Stroke s1 = new BasicStroke(1f);
        Stroke s4 = new BasicStroke(4f);

        // Drawing the finish line
        g2d.setStroke(s1);
        g2d.setColor(c3);
        g2d.draw(finishLine);
        g2d.fill(finishLine);

        // Drawing the border and the central borders
        g2d.setColor(c1);
        g2d.setStroke(s4);
        for (Line2D line : borderLines) {
            g2d.draw(line);
        }
        g2d.draw(centralBorder1);
        g2d.draw(centralBorder2);

        // Filling the central borders
        g2d.setColor(c2);
        g2d.setStroke(s1);
        g2d.fillRect(241, 241, 77, 157);
        g2d.fillRect(961, 241, 77, 237);

        g2d.setStroke(s1);
        g2d.setColor(c4);

        // Drawing the actual karts
        add(redKartImg);
        redKartImg.setBounds(redKart.x, redKart.y, 50, 50);
        redKartImg.setIcon(redKart.currImg);


        add(greenKartImg);
        greenKartImg.setBounds(greenKart.x, greenKart.y, 50, 50);
        greenKartImg.setIcon(greenKart.currImg);

        // Draw hitboxes (for testing purposes)
        /*
        g2d.draw(redKart.hitBox);
        g2d.draw(greenKart.hitBox);
         */
    }


    // Method used to check and start the timer used for drawing our components to the screen
    public void startAnimation() {

        super.requestFocusInWindow();

        if (kartAnimation == null) {
            // Timer used to determine the refresh rate (60 fps)
            kartAnimation = new Timer(1000 / 60, e -> actionPerformed());
            kartAnimation.start();
        } else {
            if (!kartAnimation.isRunning())
                kartAnimation.restart();
        }
    }

    // Method triggered every frame interval
    private void actionPerformed() {

        // Checks the game state
        switch (gameState) {

            // -1 denotes not initiated
            case -1:
                readyToggleButton.setFont(new Font("Tahoma", Font.PLAIN, 20));
                readyToggleButton.setBounds(20, 20, 140, 45);
                readyToggleButton.addItemListener(this);
                readyToggleButton.setText("NOT READY");
                readyToggleButton.setForeground(Color.RED);
                readyToggleButton.setVisible(false);

                messageLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
                messageLabel.setBounds(100,30, 1100,40);;
                messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                messageLabel.setVisible(true);
                messageLabel.setText("Awaiting for an opponent");

                break;

            // 0 denotes the game is on hold
            case 0:
                readyToggleButton.setVisible(true);
                messageLabel.setText("Waiting for all players to be ready");
                break;

            // 1 denotes the game is running
            case 1:
                if (!running && !preparing) {
                    preparing = true;
                    new Thread(this::startGame).start();
                }
                checkKartState();
                repaint();
                break;

            // 2 denotes the game is finished
            case 2:
                running = false;
                switch (MenuPanel.clientID){
                    case 0:
                        if(redKart.lapsMade == 3)
                            messageLabel.setText("YOU HAVE WON!");
                        else if(greenKart.lapsMade == 3)
                            messageLabel.setText("You have lost.");
                        else
                            messageLabel.setText("DRAW. Karts have crashed in each other");
                        break;

                    case 1:
                        if(greenKart.lapsMade == 3)
                            messageLabel.setText("YOU HAVE WON!");
                        else if(redKart.lapsMade == 3)
                            messageLabel.setText("You have lost.");
                        else
                            messageLabel.setText("DRAW. Karts have crashed in each other");
                        break;
                }
                break;
        }

    }

    private void startGame() {
        readyToggleButton.setVisible(false);
        messageLabel.setForeground(Color.orange);
        messageLabel.setText("Get Ready!");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { }

        messageLabel.setForeground(Color.yellow);
        messageLabel.setText("Get Set!");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { }

        messageLabel.setForeground(Color.green);
        messageLabel.setText("GO!!");
        running = true;
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { }

        messageLabel.setForeground(Color.black);
        while(running) {
            int laps = -1;
            switch (MenuPanel.clientID){
                case 0-> laps = redKart.lapsMade;
                case 1-> laps = greenKart.lapsMade;
            }
            messageLabel.setText("Lap: " + (laps + 1) + " / 3");
            try { Thread.sleep(500); } catch (InterruptedException ignored) { }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    // Method triggered when a key is pressed
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Depending on the ownership of the kart, the key affects the specific owned kart
        switch (MenuPanel.clientID) {
            case 0 -> KartControl(redKart, key);
            case 1 -> KartControl(greenKart, key);
        }
    }

    // Method used to process the key pressed
    private void KartControl(Kart kart, int key) {

        // Verifies that the game is running and the kart is still movable
        if (!kart.hasCrashed && running) {
            // It accepts both mappings of keys (arrow keys and WASD)
            switch (key) {
                case KeyEvent.VK_A, KeyEvent.VK_LEFT -> kart.RotateLeft();
                case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> kart.RotateRight();
                case KeyEvent.VK_W, KeyEvent.VK_UP -> kart.Accelerate();
                case KeyEvent.VK_S, KeyEvent.VK_DOWN -> kart.Decelerate();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    // Verifies if the owned kart need to be sent to the server
    public void checkKartState() {

        Kart ownKart = null;

        // Retrieve the kart
        switch (MenuPanel.clientID) {
            case 0 -> ownKart = redKart;
            case 1 -> ownKart = greenKart;
            default -> RunGameTest.logger.severe("Application Exception ID 303 \n Invalid ID provided");
        }

        // We determine if a kart needs to be pushed to the server if the current version and the updated version dont
        // have the same hitbox anymore
        try {
            assert ownKart != null;
            Kart check = ownKart.cloneKart();
            ownKart.UpdatePosition();
            if (ownKart.hitBox != check.hitBox) {
                ownKart.syncRequired = true;
            }
        } catch (CloneNotSupportedException e) {
            RunGameTest.logger.severe("Unexpected Exception ID 902 \n" + e.getMessage() );
        }

    }

    // Method triggered when the ready button is clicked
    @Override
    public void itemStateChanged(ItemEvent e) {

        // If the player has indicated that he is ready, a message will be sent to the server
        if (e.getSource() == readyToggleButton && readyToggleButton.isVisible()) {
            if (readyToggleButton.isSelected()) {
                readyToggleButton.setText("READY");
                readyToggleButton.setForeground(Color.GREEN);
                MenuPanel.clientConnector.sendData("ready");

                // In a similar way, we will sent a message to the server if the player indicated that he is no
                // longer ready
            } else if (!readyToggleButton.isSelected()) {
                readyToggleButton.setText("NOT READY");
                readyToggleButton.setForeground(Color.RED);
                MenuPanel.clientConnector.sendData("not_ready");
            }
        }
    }
}