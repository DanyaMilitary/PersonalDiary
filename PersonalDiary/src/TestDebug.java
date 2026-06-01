import javax.swing.*;

public class TestDebug {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DebugPanel debugPanel = new DebugPanel();
            debugPanel.setVisible(true);
        });
    }
}