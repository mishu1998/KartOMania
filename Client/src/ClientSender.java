import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientSender extends Thread {

    private final Socket socket;
    Object data;
    private OutputStream outputStream;
    private ObjectOutputStream objectOutputStream;

    // Initialize the sender thread
    public ClientSender(Socket socket) {
        this.socket = socket;

        try {
            outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
        } catch (IOException e) {
            RunGameTest.logger.severe("IO Exception ID 104 \n" + e.getMessage() );
        }
    }

    // Sets data to be sent
    public void setData(Object data) {
        this.data = data;
    }

    // Starts the sender thread
    @Override
    public void run() {
        while (RunGameTest.serverIsAlive) {

            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {
            }

            if (data != null) {
                try {
                    objectOutputStream.writeObject(data);
                    objectOutputStream.reset();
                } catch (IOException e) {
                    RunGameTest.logger.severe("IO Exception ID 105 \n" + e.getMessage() );
                } finally {
                    data = null;
                }
            }
        }
    }
}
