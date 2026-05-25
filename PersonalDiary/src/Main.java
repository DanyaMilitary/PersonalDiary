import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Устанавливаем LookAndFeel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Инициализация базы данных
        System.out.println("Инициализация базы данных...");
        Database.initialize();

        // Запуск окна авторизации
        SwingUtilities.invokeLater(() -> {
            LoginUI loginUI = new LoginUI();
            loginUI.setVisible(true);
        });
    }
}