import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;

public class Logger {
    private static final String LOG_FILE = "diary_app.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static boolean debugMode = true;

    public enum Level {
        DEBUG("DEBUG"),
        INFO("INFO"),
        WARNING("WARNING"),
        ERROR("ERROR"),
        SUCCESS("SUCCESS");

        private String prefix;
        Level(String prefix) { this.prefix = prefix; }
        public String getPrefix() { return prefix; }
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
        info("Debug mode " + (enabled ? "enabled" : "disabled"));
    }

    public static void debug(String message) {
        if (debugMode) {
            log(Level.DEBUG, message);
        }
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void warning(String message) {
        log(Level.WARNING, message);
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }

    public static void error(String message, Exception e) {
        log(Level.ERROR, message + " - " + e.getMessage());
        if (debugMode && e != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            debug("Stack trace: " + e.getClass().getSimpleName());
        }
    }

    public static void success(String message) {
        log(Level.SUCCESS, message);
    }

    private static void log(Level level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logMessage = String.format("[%s] %s: %s", timestamp, level.getPrefix(), message);

        // Write to console
        System.out.println(logMessage);

        // Write to file with UTF-8
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(LOG_FILE, true), StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(logMessage);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    public static void printSeparator(String title) {
        StringBuilder sb = new StringBuilder("\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append("=== ").append(title).append("\n");
        sb.append("=".repeat(60));
        info(sb.toString());
    }
}