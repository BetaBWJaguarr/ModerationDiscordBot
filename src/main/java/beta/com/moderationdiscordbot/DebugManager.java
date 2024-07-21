package beta.com.moderationdiscordbot;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DebugManager {

    private static final Logger logger = Logger.getLogger(DebugManager.class.getName());

    public static void logDebug(String message) {
        logger.log(Level.INFO, message);
    }

    public static void logWarning(String message) {
        logger.log(Level.WARNING, "\u001B[33m" + message + "\u001B[0m");
    }

    public static void logError(String message) {
        logger.log(Level.SEVERE, "\u001B[31m" + message + "\u001B[0m");
    }
}
