import javax.swing.*;
import java.awt.*;
import java.lang.management.*;
import java.text.DecimalFormat;

public class SystemMonitor extends JFrame {
    private JLabel memoryLabel;
    private JLabel cpuLabel;
    private JLabel threadsLabel;
    private JLabel uptimeLabel;
    private Timer updateTimer;
    private DecimalFormat df = new DecimalFormat("#.##");
    private long startTime;

    public SystemMonitor() {
        setTitle("System Monitor");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        startTime = System.currentTimeMillis();

        JPanel mainPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        memoryLabel = new JLabel();
        memoryLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));

        cpuLabel = new JLabel();
        cpuLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));

        threadsLabel = new JLabel();
        threadsLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));

        uptimeLabel = new JLabel();
        uptimeLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));

        mainPanel.add(memoryLabel);
        mainPanel.add(cpuLabel);
        mainPanel.add(threadsLabel);
        mainPanel.add(uptimeLabel);

        add(mainPanel);

        updateTimer = new Timer(1000, e -> updateStats());
        updateTimer.start();

        updateStats();
    }

    private void updateStats() {
        // Memory
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        memoryLabel.setText(String.format("Memory: %.2f MB / %.2f MB (%.1f%%)",
                usedMemory / 1024.0 / 1024.0,
                maxMemory / 1024.0 / 1024.0,
                (usedMemory * 100.0) / maxMemory));

        // CPU
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getSystemLoadAverage();
        cpuLabel.setText(String.format("CPU Load: %.2f", cpuLoad));

        // Threads
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int threadCount = threadBean.getThreadCount();
        int peakThreadCount = threadBean.getPeakThreadCount();
        threadsLabel.setText(String.format("Threads: %d (peak: %d)", threadCount, peakThreadCount));

        // Uptime
        long uptime = (System.currentTimeMillis() - startTime) / 1000;
        long hours = uptime / 3600;
        long minutes = (uptime % 3600) / 60;
        long seconds = uptime % 60;
        uptimeLabel.setText(String.format("Uptime: %02d:%02d:%02d", hours, minutes, seconds));
    }

    @Override
    public void dispose() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
        super.dispose();
    }
}