import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class DebugPanel extends JFrame {
    private JTextArea logArea;
    private JButton refreshButton;
    private JButton clearButton;
    private JButton exportButton;
    private JButton toggleDebugButton;
    private Timer refreshTimer;
    private boolean autoRefresh = true;

    public DebugPanel() {
        setTitle("Debug Panel");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshLog());

        clearButton = new JButton("Clear Log");
        clearButton.addActionListener(e -> clearLog());

        exportButton = new JButton("Export");
        exportButton.addActionListener(e -> exportLog());

        toggleDebugButton = new JButton("Debug: " + (Logger.isDebugMode() ? "ON" : "OFF"));
        toggleDebugButton.addActionListener(e -> toggleDebugMode());

        JCheckBox autoRefreshCheck = new JCheckBox("Auto-refresh", autoRefresh);
        autoRefreshCheck.addActionListener(e -> setAutoRefresh(autoRefreshCheck.isSelected()));

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> setVisible(false));

        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(toggleDebugButton);
        buttonPanel.add(autoRefreshCheck);
        buttonPanel.add(closeButton);

        // Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new TitledBorder("Application Log"));

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        JLabel statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Timer for auto-refresh
        refreshTimer = new Timer(2000, e -> {
            if (autoRefresh && isVisible()) {
                refreshLog();
            }
        });
        refreshTimer.start();

        // Load log on startup
        refreshLog();
    }

    private void refreshLog() {
        try {
            File logFile = new File("diary_app.log");
            if (logFile.exists()) {
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(logFile), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                logArea.setText(content.toString());
                // Scroll to bottom
                logArea.setCaretPosition(logArea.getDocument().getLength());
            } else {
                logArea.setText("Log file not created yet");
            }
        } catch (IOException e) {
            logArea.setText("Error reading log file: " + e.getMessage());
        }
    }

    private void clearLog() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear the log file?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (FileWriter fw = new FileWriter("diary_app.log")) {
                fw.write("");
                Logger.info("Log file cleared by user");
                refreshLog();
                JOptionPane.showMessageDialog(this, "Log file cleared!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error clearing log: " + e.getMessage());
            }
        }
    }

    private void exportLog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("diary_log_export_" +
                new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File exportFile = fileChooser.getSelectedFile();
            try {
                java.nio.file.Files.copy(new File("diary_app.log").toPath(),
                        exportFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                Logger.success("Log exported to: " + exportFile.getName());
                JOptionPane.showMessageDialog(this, "Log exported successfully!");
            } catch (IOException e) {
                Logger.error("Error exporting log", e);
                JOptionPane.showMessageDialog(this, "Export error: " + e.getMessage());
            }
        }
    }

    private void toggleDebugMode() {
        boolean newMode = !Logger.isDebugMode();
        Logger.setDebugMode(newMode);
        toggleDebugButton.setText("Debug: " + (newMode ? "ON" : "OFF"));
    }

    private void setAutoRefresh(boolean enabled) {
        autoRefresh = enabled;
        if (autoRefresh) {
            refreshTimer.start();
        } else {
            refreshTimer.stop();
        }
    }
}