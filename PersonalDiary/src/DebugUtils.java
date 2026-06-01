import java.sql.*;
import java.util.*;
import javax.swing.*;

public class DebugUtils {

    // Test database connection
    public static boolean testDatabaseConnection() {
        Logger.debug("Testing database connection...");
        try {
            Connection conn = Database.getConnection();
            if (conn != null && !conn.isClosed()) {
                Logger.success("Database connection established successfully");
                return true;
            } else {
                Logger.error("Database connection is not active");
                return false;
            }
        } catch (SQLException e) {
            Logger.error("Database connection error", e);
            return false;
        }
    }

    // Print table information
    public static void printTableInfo() {
        Logger.debug("Getting table information...");
        try {
            DatabaseMetaData metaData = Database.getConnection().getMetaData();
            String[] tableTypes = {"TABLE"};
            ResultSet tables = metaData.getTables(null, null, "%", tableTypes);

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                Logger.debug("Table: " + tableName);

                // Count records
                try (Statement stmt = Database.getConnection().createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
                    if (rs.next()) {
                        Logger.debug("  - Records: " + rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            Logger.error("Error getting table information", e);
        }
    }

    // Profile method execution
    public static void profile(Runnable task, String taskName) {
        long startTime = System.nanoTime();
        try {
            task.run();
            long endTime = System.nanoTime();
            double durationMs = (endTime - startTime) / 1_000_000.0;
            Logger.debug(String.format("%s completed in %.2f ms", taskName, durationMs));
        } catch (Exception e) {
            Logger.error("Error executing " + taskName, e);
        }
    }

    // Check EDT thread
    public static void checkEDT() {
        if (SwingUtilities.isEventDispatchThread()) {
            Logger.debug("Executing in EDT (correct)");
        } else {
            Logger.warning("Executing NOT in EDT!");
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            Logger.debug("Call stack:");
            for (int i = 2; i < Math.min(10, stack.length); i++) {
                Logger.debug("  " + stack[i]);
            }
        }
    }

    // Simulate load for testing
    public static void simulateLoad(int milliseconds) {
        Logger.debug("Simulating load for " + milliseconds + " ms...");
        long endTime = System.currentTimeMillis() + milliseconds;
        int counter = 0;
        while (System.currentTimeMillis() < endTime) {
            counter++;
        }
        Logger.debug("Load simulation completed. Iterations: " + counter);
    }

    // Generate test data
    public static void generateTestData() {
        Logger.info("Generating test data...");
        // Add test data generation logic here
    }
}
