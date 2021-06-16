import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ServerSender extends Thread {

    private final Socket socket;
    private OutputStream outputStream;
    private ObjectOutputStream objectOutputStream;

    private Object data;

    // Initialise the sender socket
    public ServerSender(Socket socket) {
        this.socket = socket;

        try {
            outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
        } catch (IOException e) {
            TCPserver.logger.severe("IO Exception ID 104 \n" + e.getMessage() );
        }
    }

    // Sets the data to be sent
    public void setData(Object data) {
        this.data = data;
    }

    // Starts the sender thread and sends any pending data
    @Override
    public void run() {
        while (socket.isConnected()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {
            }
            if (data != null) {
                try {
                    objectOutputStream.writeObject(data);
                    objectOutputStream.flush();
                    objectOutputStream.reset();
                } catch (IOException e) {
                    TCPserver.logger.severe("IO Exception ID 105 \n" + e.getMessage() );
                } finally {
                    data = null;
                }
            }
        }
    }
}
