import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantLock;

public class ServerListener extends Thread {

    public static List<Kart> clientsKart = new ArrayList<Kart>();
    public static boolean[] clientReady = {false, false};
    private final Socket socket;
    private final ReentrantLock lock = new ReentrantLock();
    private final ServerSender serverSender;
    private final GameHandler gameHandler = new GameHandler();
    private int clientID = -1;
    private String lastCommand;

    // Initialise the socket for the client communication
    public ServerListener(Socket socket) {
        this.socket = socket;

        // Start the sender thread
        serverSender = new ServerSender(socket);
        serverSender.start();
    }

    // Initialise and start listening for data coming from the client
    @Override
    public void run() {
        try {
            ObjectInputStream objectInputStream;
            InputStream inputStream;

            inputStream = socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);

            // Asses and handle the type of data received accordingly
            do {
                Object data = objectInputStream.readObject();
                if (data instanceof String) {
                    handleCommand((String) data);
                } else if (data instanceof Kart) {
                    handleKart((Kart) data);
                } else {
                    TCPserver.logger.warning("Client " + clientID + " sent unrecognisable data.\n" + data.toString());
                }
            } while (socket.isConnected());

        } catch (IOException | ClassNotFoundException e) {
            TCPserver.logger.severe("General IO Exception ID 102 \n" + e.getMessage() );
        }
    }

    // Method which handles the kart received
    private synchronized void handleKart(Kart receivedKart) {

        boolean kartExists = false;

        TCPserver.logger.info("Received from Client #" + clientID + "\n Kart: " + receivedKart.x + "|" + receivedKart.y);

        lock.lock();

        // Checks the collision of the kart
        receivedKart = gameHandler.check(receivedKart);

        // This block is responsible for the handling of the kart, internally in the server
        // Replacing the server version of the kart with the most up-to-date version
        try {
            for (ListIterator<Kart> iterator = clientsKart.listIterator(); iterator.hasNext(); ) {
                Kart currentClientKart = iterator.next();
                if (currentClientKart == null) {
                    TCPserver.logger.warning("Server's Kart was null");
                    continue;
                }
                if (currentClientKart.getID() == receivedKart.getID()) {
                    iterator.set(receivedKart);
                    kartExists = true;
                }
                // If any kart has satisfied the winning condition, sends a command to the client denoting that the game
                // has ended
                if(currentClientKart.lapsMade == 3 || currentClientKart.hasCrashed) {
                    currentClientKart.syncToClient = currentClientKart.syncRequired = true;
                    send("finished");
                }
            }
            if (!kartExists) {
                ListIterator<Kart> iterator = clientsKart.listIterator();
                iterator.add(receivedKart);
            }
        } catch (Exception e) {
            TCPserver.logger.severe("Unexpected Exception ID 901 \n" + e.getMessage() );
        } finally {
            lock.unlock();
        }
    }

    // Method used to handle the commands received from the client
    private void handleCommand(String command) {

        TCPserver.logger.info("From Client #" + clientID + " : " + command);

        String[] responseParts = command.split(" ");

        switch (responseParts[0]) {

            // This command initialises and sets the kart ownership of a kart based on the ID received
            case "identify":

                clientID = Integer.parseInt(responseParts[1]);

                (new Thread(() -> {
                    waitForConnections();
                    waitForReady();
                    startGame();
                })).start();

                break;

            // Sets the flag showing the client is ready
            case "ready":
                clientReady[clientID] = true;
                break;

            // Sets the flag showing the client is not ready
            case "not_ready":
                clientReady[clientID] = false;
                break;

            // Resends the last command if the client received a malformed or corrupted packet
            case "unknown_command":
                if (lastCommand != null)
                    send(lastCommand);
                else {
                    TCPserver.logger.severe("Communication Exception ID 201");
                    return;
                }
                break;

            // Sends a command denoting that the server could not process the command received from the client
            default:
                TCPserver.logger.severe("Command " + command + " not recognised");
                send("unknown_command");
                break;
        }
    }

    // Method used to assess if both clients are connected, otherwise it waits until the condition is satisfied
    private void waitForConnections() {
        send("wait");
        if (clientsKart.toArray().length != 2) {
            while (clientsKart.toArray().length != 2) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    // Method used to asses if both clients are ready, otherwise it waits until the condition is satisfied
    private void waitForReady() {
        send("ready");

        boolean allClientsReady = false;

        while (!allClientsReady) {
            allClientsReady = true;
            for (boolean ready : clientReady) {
                if (!ready) {
                    allClientsReady = false;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {
                    }
                    break;
                }
            }
        }
        Thread.yield();
    }

    // Method which starts the active communication and transmission of the foreign kart to the client
    private void startGame() {
        send("start");
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {
        }

        // Determines whenever the own kart or foreign kart needs to be sent back to the client based on the flags
        while (clientReady[clientID]) {
            lock.lock();
            try {
                for (ListIterator<Kart> iterator = clientsKart.listIterator(); iterator.hasNext(); ) {
                    Kart kart = iterator.next();
                    if (kart.getID() != clientID && kart.syncRequired) {
                        kart.syncRequired = false;
                        send(kart);
                    }
                    if (kart.getID() == clientID && kart.syncToClient) {
                        kart.syncToClient = false;
                        send(kart);
                    }
                }
            } catch (Exception e) {
                TCPserver.logger.severe("Unexpected Exception ID 901 \n" + e.getMessage() );
            } finally {
                lock.unlock();
            }
            try {
                Thread.sleep(1000 / 60);
            } catch (InterruptedException ignored) {
            }
        }
    }

    // Sends data to the sender thread
    private void send(Object data) {

        serverSender.setData(data);

        if (data instanceof String) {
            lastCommand = (String) data;
            TCPserver.logger.info("Command: " + lastCommand + " sent to client #" + clientID);
        } else {
            TCPserver.logger.info("Kart #" + ((Kart) data).getID() + " sent to client #" + clientID);
        }
    }
}

