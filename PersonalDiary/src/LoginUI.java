import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class LoginUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginUI() {
        setTitle("Вход в систему");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 250));

        JLabel titleLabel = new JLabel("📘 Личный дневник", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(41, 128, 185));
        titleLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 245, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel usernameLabel = new JLabel("Логин:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);

        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Пароль:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(passwordField, gbc);

        JButton loginBtn = new JButton("Войти");
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loginBtn.setBackground(new Color(46, 204, 113));
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setPreferredSize(new Dimension(100, 35));
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(loginBtn, gbc);

        add(formPanel, BorderLayout.CENTER);

        loginBtn.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            int[] result = Database.login(username, password);
            if (result != null) {
                int role = result[1];
                int id = result[0];
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    switch (role) {
                        case 1 -> new DirectorUI(id).setVisible(true);
                        case 2 -> new TeacherUI(id).setVisible(true);
                        case 3 -> new StudentUI(id, username).setVisible(true);
                        default -> JOptionPane.showMessageDialog(null, "Неизвестная роль пользователя");
                    }
                });
            } else {
                JOptionPane.showMessageDialog(null, "Неверный логин или пароль", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }

}