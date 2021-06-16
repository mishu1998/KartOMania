import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


public class ClientConnector extends Thread {

    private static final int PORT = 5000;
    public static Socket socket;
    public boolean connected = false;
    public boolean waiting = true;
    private final String serverAddress;
    private ClientListener clientListener;

    // Sets the server ip
    public ClientConnector(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    // Method used to set the data that is needed to be sent
    public void sendData(Object data) {
        clientListener.send(data);
    }

    // Attempts to establish a connection to the IP provided
    @Override
    public void run() {
        if (socket == null && serverAddress != null) {
            waiting = true;
            try {
                socket = new Socket(serverAddress, PORT);
                connected = true;
            } catch (UnknownHostException e) {
                RunGameTest.logger.warning("Communication Exception ID 202 \n Could not connect to server " + serverAddress + ":" + PORT);
                connected = false;
            } catch (IOException e) {
                RunGameTest.logger.warning("IO Exception ID 104 \n Couldn't get I/O for the connection to: " + serverAddress + ":" + PORT);
                connected = false;
            }

            waiting = false;
            // If the connection was successful, starts a listening thread
            if (connected) {
                RunGameTest.serverIsAlive = true;
                RunGameTest.logger.info("Client established connection to server " + serverAddress + ":" + PORT);

                clientListener = null;
                clientListener = new ClientListener(socket);
                clientListener.start();
            }

        }
    }

}
