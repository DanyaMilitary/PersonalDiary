import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public abstract class AbstractUserUI extends JFrame {
    protected int userId;
    protected JPanel infoPanel;

    public AbstractUserUI(String title, int userId) {
        this.userId = userId;
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10,10));
        mainPanel.setBackground(Color.decode("#F5F5FA"));
        mainPanel.setBorder(new EmptyBorder(15,15,15,15));

        JPanel topPanel = createTopPanel(title);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        infoPanel = new JPanel(new GridLayout(1,3,15,0));
        infoPanel.setBorder(new EmptyBorder(20,20,20,20));
        infoPanel.setBackground(Color.decode("#F5F5FA"));
        refreshInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.CENTER);

        JTabbedPane tabbedPane = createTabbedPane();
        mainPanel.add(tabbedPane, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createTopPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#2980B9"));
        panel.setBorder(new EmptyBorder(15,20,15,20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Выход");
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 12));
        logoutBtn.setBackground(Color.decode("#E74C3C"));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setPreferredSize(new Dimension(100,35));
        logoutBtn.addActionListener(e -> {
            new LoginUI().setVisible(true);
            dispose();
        });
        panel.add(logoutBtn, BorderLayout.EAST);
        return panel;
    }

    protected JPanel createCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                new EmptyBorder(10,10,10,10)
        ));
        card.setPreferredSize(new Dimension(180,100));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(color);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 20));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    protected abstract void refreshInfoPanel();
    protected abstract JTabbedPane createTabbedPane();
}