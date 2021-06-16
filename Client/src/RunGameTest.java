import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

class RunGameTest {

    // Variables used to store the menu window and logger
    public static MenuWindow mw;
    public static Logger logger;

    // Variable used to check if the server is alive
    public static boolean serverIsAlive = false;

    // Start of the program
    public static void main(String[] arg) {

        StartLogger();
        mw = new MenuWindow();

    }

    // Initialise the logger
    private static void StartLogger() {

        // Sets the format on which the timestamp will be recorded on the logger
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
        LocalDateTime dateTime = LocalDateTime.now();
        String formattedDateTime = dateTime.format(dateTimeFormatter);

        logger = Logger.getLogger("Client_logger");
        FileHandler fh;

        // Checks if the folder for logs exists, if not, it creates a new one
        File directory = new File("logs");
        if (!directory.exists()) {
            directory.mkdir();
        }

        try {
            // Configure the logger with handler and formatter
            fh = new FileHandler("logs/" + formattedDateTime + "_client.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            logger.info("Application Started");

        } catch (SecurityException | IOException e) {
            logger.severe("General IO Exception ID 103 \n" + e.getMessage() );
        }

    }
}