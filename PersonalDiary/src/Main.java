import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Main {
    public static void main(String[] args) {
        // Включаем режим отладки
        Logger.setDebugMode(true);
        Logger.printSeparator("ЗАПУСК ПРИЛОЖЕНИЯ");

        // Устанавливаем LookAndFeel
        try {
            Logger.debug("Установка LookAndFeel...");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Logger.success("LookAndFeel установлен");
        } catch (Exception e) {
            Logger.error("Ошибка установки LookAndFeel", e);
        }

        // Инициализация базы данных
        Logger.info("Инициализация базы данных...");
        DebugUtils.profile(() -> {
            Database.initialize();
        }, "Инициализация БД");

        // Проверка соединения
        if (!DebugUtils.testDatabaseConnection()) {
            Logger.error("Не удалось подключиться к БД. Приложение может работать некорректно.");
            int result = JOptionPane.showConfirmDialog(null,
                    "Не удалось подключиться к базе данных. Продолжить?",
                    "Ошибка БД",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE);
            if (result != JOptionPane.YES_OPTION) {
                System.exit(1);
            }
        }

        // Вывод информации о таблицах
        DebugUtils.printTableInfo();

        // Запуск окна авторизации
        SwingUtilities.invokeLater(() -> {
            DebugUtils.checkEDT();
            Logger.info("Запуск окна авторизации...");

            // Добавляем горячую клавишу для отладки (Ctrl+Shift+D)
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .addKeyEventDispatcher(e -> {
                        if (e.isControlDown() && e.isShiftDown() && e.getKeyCode() == 68) { // D
                            openDebugTools();
                            return true;
                        }
                        return false;
                    });

            LoginUI loginUI = new LoginUI();
            loginUI.setVisible(true);
            Logger.info("Приложение запущено");
        });
    }

    private static void openDebugTools() {
        Logger.info("Открытие отладочных инструментов");

        JFrame debugFrame = new JFrame("Отладочные инструменты");
        debugFrame.setSize(300, 200);
        debugFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton logButton = new JButton("📋 Показать лог");
        logButton.addActionListener(e -> {
            DebugPanel debugPanel = new DebugPanel();
            debugPanel.setVisible(true);
        });

        JButton monitorButton = new JButton("📊 Системный монитор");
        monitorButton.addActionListener(e -> {
            SystemMonitor monitor = new SystemMonitor();
            monitor.setVisible(true);
        });

        JButton testDbButton = new JButton("🗄️ Тест БД");
        testDbButton.addActionListener(e -> {
            DebugUtils.testDatabaseConnection();
            DebugUtils.printTableInfo();
            JOptionPane.showMessageDialog(debugFrame, "Тест БД выполнен. Проверьте консоль.");
        });

        JButton heapButton = new JButton("💾 Инфо о памяти");
        heapButton.addActionListener(e -> {
            Runtime rt = Runtime.getRuntime();
            String info = String.format(
                    "Max Memory: %.2f MB\n" +
                            "Total Memory: %.2f MB\n" +
                            "Free Memory: %.2f MB\n" +
                            "Used Memory: %.2f MB",
                    rt.maxMemory() / 1024.0 / 1024.0,
                    rt.totalMemory() / 1024.0 / 1024.0,
                    rt.freeMemory() / 1024.0 / 1024.0,
                    (rt.totalMemory() - rt.freeMemory()) / 1024.0 / 1024.0
            );
            JOptionPane.showMessageDialog(debugFrame, info, "Информация о памяти",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        panel.add(logButton);
        panel.add(monitorButton);
        panel.add(testDbButton);
        panel.add(heapButton);

        debugFrame.add(panel);
        debugFrame.setVisible(true);

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    if (e.isControlDown() && e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_D) {
                        openDebugTools();
                        return true;
                    }
                    return false;
                });
    }
}