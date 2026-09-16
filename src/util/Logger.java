package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * Simple file-based logger.
 * Addresses the Logging/Monitoring non-functional requirement.
 */
public class Logger {

    private static final String LOG_FILE = "data/application.log";

    public static void info(String message) {
        write("INFO", message);
    }

    public static void warn(String message) {
        write("WARN", message);
    }

    public static void error(String message) {
        write("ERROR", message);
    }

    private static synchronized void write(String level, String message) {
        String line = String.format("[%s] [%s] %s", LocalDateTime.now(), level, message);
        System.out.println(line);
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            pw.println(line);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
}
