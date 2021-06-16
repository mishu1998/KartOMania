import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class ServerConnector extends Thread {
    static final int PORT = 5000;
    public static ServerSocket serverSocket;
    public static HashMap<Socket, String> users = new HashMap();
    public static boolean serverAlive = false;
    public static boolean bothUsersConnected = false;

    public void run() {
        // Attempts to bind to our desired port number
        if (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(PORT);
                serverAlive = true;

                TCPserver.logger.info("Server started.");

                // Accepts incoming connections as long as we do not have two clients connected
                while (!bothUsersConnected) {
                    Socket clientSocket = serverSocket.accept();

                    TCPserver.logger.info("Incoming connection from " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

                    // Start the listener thread for each client
                    ServerListener serverListener = new ServerListener(clientSocket);
                    serverListener.start();

                    // Verifies if the condition of two clients has been satisfied
                    users.put(clientSocket, null);
                    if (users.size() == 2) {
                        bothUsersConnected = true;
                    }
                }
            } catch (IOException e) {
                TCPserver.logger.severe("General IO Exception ID 101 \n" + e.getMessage() );
            }
        }
    }

}
