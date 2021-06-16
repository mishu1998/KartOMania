import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ClientListener extends Thread {

    private final Socket socket;
    private final ClientSender clientSender;
    private String lastCommand;

    // Start a sender thread and it sends initial data required by the server
    public ClientListener(Socket socket) {
        this.socket = socket;

        clientSender = new ClientSender(socket);
        clientSender.start();

        String initialCommand = "identify " + MenuPanel.clientID;
        send(initialCommand);
        sendOwnKart();
    }

    // Actively listens for any communication coming from the server
    @Override
    public void run() {
        try {
            ObjectInputStream objectInputStream;
            InputStream inputStream;

            inputStream = socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);

            do {
                Object data = objectInputStream.readObject();

                // Passes the data received accordingly to their handle methods
                if (data instanceof String) {
                    handleCommand((String) data);
                }
                else if (data instanceof Kart) {
                    handleKart((Kart) data);
                } else {
                RunGameTest.logger.warning("Server has sent unrecognisable data.\n" + data.toString());
                }
            } while (socket.isConnected());
        } catch (IOException | ClassNotFoundException e) {

            RunGameTest.logger.severe("General IO Exception ID 102 \n" + e.getMessage() );
            JOptionPane.showMessageDialog(null,"Server has disconnected. \nPlease reconnect.", "Warning", JOptionPane.WARNING_MESSAGE);

            RunGameTest.serverIsAlive = false;

            MenuPanel.gameWindow.dispose();
            for (Thread t : Thread.getAllStackTraces().keySet())
            {  if (t.getState()==Thread.State.RUNNABLE)
                t.interrupt();
            }
            RunGameTest.mw = new MenuWindow();
        }
    }

    // Method used to handle received karts
    private void handleKart(Kart kart) {

        RunGameTest.logger.info("Received kart from Server \n Kart ID " + kart.getID() + " : " + kart.x + " | " + kart.y);

        // This replaces the current kart with the one received from the server
        kart.Generate();
        switch (kart.getID()) {
            case 0 -> KartAnimationPanel.setRedKart(kart);
            case 1 -> KartAnimationPanel.setGreenKart(kart);
        }

        // Bug fix (explained in documentation)
        if(kart.hasCrashed)
            KartAnimationPanel.gameState = 2;
        Thread.yield();
    }

    // Method used to handle commands from the server
    private void handleCommand(String command) {

        RunGameTest.logger.info("Received command from Server: " + command);

        switch (command) {

            case "start":

                // Sets the game state to 'running'
                KartAnimationPanel.gameState = 1;
                Thread.yield();

                // Starts a new thread which will continuously attempt to send client's kart to the server
                (new Thread(() -> {
                    do {
                        sendOwnKart();
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (KartAnimationPanel.gameState == 1);
                })).start();

                break;

            // Sets the game state to 'uninitialized'
            case "wait":
                KartAnimationPanel.gameState = -1;
                RunGameTest.logger.info("Game State changed to  " + KartAnimationPanel.gameState);
                break;

            // Sets the game state to 'ready'
            case "ready":
                KartAnimationPanel.gameState = 0;
                RunGameTest.logger.info("Game State changed to  " + KartAnimationPanel.gameState);
                break;


            // In case of malformed packets, server will send this command which in turn will send the last command
            // which the client has attempted to send
            case "unknown_command":
                if (lastCommand != null)
                    send(lastCommand);
                else {
                    RunGameTest.logger.severe("Communication Exception ID 201");
                    return;
                }
                break;

            // Sets the game state to 'finished'
            case "finished":
                KartAnimationPanel.gameState = 2;
                RunGameTest.logger.info("Game State changed to  " + KartAnimationPanel.gameState);
                break;

            // Default case, when the client can't recognise what the server sent
            default:
                RunGameTest.logger.severe("Command " + command + " not recognised");
                send("unknown_command");
                break;
        }
    }

    // Method used to send data to the server
    public void send(Object data) {

        clientSender.setData(data);

        if (data instanceof String) {
            lastCommand = (String) data;
            RunGameTest.logger.info("Command " + lastCommand + " has been sent to the server");
        } else {
            Kart lastKart = (Kart) data;
            RunGameTest.logger.info("Kart ID " + lastKart.getID() + " : " + lastKart.x + " | " + lastKart.y + " has been sent to the server");
        }
    }

    // Method used to determine which kart has to be sent to the server and if it is needed
    private synchronized void sendOwnKart() {
        Kart kart = null;

        switch (MenuPanel.clientID) {
            case 0 -> kart = KartAnimationPanel.getRedKart();
            case 1 -> kart = KartAnimationPanel.getGreenKart();
        }

        // If the kart has no changes since the last update, the kart will not be sent
        assert kart != null;
        if (kart.syncRequired) {
            send(kart);
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {
            }
            kart.syncRequired = false;
        }
    }
}
