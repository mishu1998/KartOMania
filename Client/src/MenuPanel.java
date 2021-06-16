import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuPanel extends JPanel implements ActionListener {

    public static ClientConnector clientConnector = null;
    public static GameWindow gameWindow;
    public static int clientID = -1;
    private final JLabel logoLabel;
    private final JLabel inputServerAddressLabel;
    private final JTextField inputServerAddressField;
    private final ButtonGroup kartSelectionButtons;
    private final JRadioButton rdbtnRedKart;
    private final JRadioButton rdbtnGreenKart;
    private final JButton connectToServerButton;

    // Initialise the panel used for the menu window
    public MenuPanel() {
        setLayout(null);

        // Logo
        logoLabel = new JLabel();
        logoLabel.setIcon(new ImageIcon(getClass().getResource("Sprites/logo.png")));
        logoLabel.setBounds(10, 10, 480, 70);
        add(logoLabel);

        // Label and field for inputting the server address
        inputServerAddressLabel = new JLabel("Server IP:");
        inputServerAddressLabel.setBounds(148, 102, 62, 23);    // x, y, w, h within this JPanel
        add(inputServerAddressLabel);

        inputServerAddressField = new JTextField();          // Create output area for local info
        inputServerAddressField.setText("localhost");
        inputServerAddressField.setBounds(210, 102, 100, 23);  // x, y, w, h within this JPanel
        inputServerAddressField.addActionListener(this);
        add(inputServerAddressField);

        // Buttons used for player selection
        kartSelectionButtons = new ButtonGroup();

        rdbtnRedKart = new JRadioButton("Player 1");
        rdbtnRedKart.setForeground(Color.RED);
        kartSelectionButtons.add(rdbtnRedKart);
        rdbtnRedKart.setBounds(204, 132, 109, 23);
        rdbtnRedKart.setSelected(true);
        add(rdbtnRedKart);

        rdbtnGreenKart = new JRadioButton("Player 2");
        rdbtnGreenKart.setForeground(new Color(0, 128, 0));
        kartSelectionButtons.add(rdbtnGreenKart);
        rdbtnGreenKart.setBounds(204, 158, 109, 23);
        add(rdbtnGreenKart);

        // Button used to connect
        connectToServerButton = new JButton("Connect");
        connectToServerButton.setBounds(204, 188, 100, 30);
        connectToServerButton.addActionListener(this);
        add(connectToServerButton);

    }

    // Render the elements
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    // Handles the interactive objects displayed
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == connectToServerButton || e.getSource() == inputServerAddressField) {
            handleButtonClick();
        }
    }

    // If the connect button is pressed or the enter key inside the input server field, this method handles those events
    private void handleButtonClick() {

        // Determine the player selection
        if (rdbtnRedKart.isSelected()) {
            clientID = 0;
        } else if (rdbtnGreenKart.isSelected()) {
            clientID = 1;
        } else {
            clientID = -1;
            RunGameTest.logger.severe("Application Exception ID 301 \n Invalid Player selection");
        }

        // Attempts to establish connection to the server provided
        clientConnector = null;
        clientConnector = new ClientConnector(inputServerAddressField.getText());
        clientConnector.start();

        // Gives time to the connection thread to establish the connection
        while(clientConnector.waiting) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }

        // Verifies that the connection to the server was established before moving forwards
        if (clientConnector.connected) {
            gameWindow = new GameWindow();
            RunGameTest.mw.dispose();
        }
        else{
            JOptionPane.showMessageDialog(null,"Unable to connect to the server! \nPlease check and try again.", "Could not connect", JOptionPane.WARNING_MESSAGE);
        }

    }

}
