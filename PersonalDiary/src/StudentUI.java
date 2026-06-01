import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class StudentUI extends JFrame {
    private int studentId;
    private String studentName;
    private JTabbedPane tabbedPane;
    private JPanel infoPanel;

    public StudentUI(int studentId, String studentName) {
        this.studentId = studentId;
        this.studentName = studentName;

        setTitle("Личный дневник - " + studentName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(245, 245, 250));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Верхняя панель
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(41, 128, 185));
        topPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("📖 Дневник ученика: " + studentName);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.black);
        topPanel.add(titleLabel, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Выход");
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 12));
        logoutBtn.setBackground(new Color(231, 76, 60));
        logoutBtn.setForeground(Color.BLACK);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setPreferredSize(new Dimension(100, 35));
        logoutBtn.addActionListener(e -> {
            new LoginUI().setVisible(true);
            dispose();
        });
        topPanel.add(logoutBtn, BorderLayout.EAST);

        // Панель информационных карточек
        infoPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        infoPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        infoPanel.setBackground(new Color(245, 245, 250));
        refreshInfoPanel();

        // Вкладки
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 13));
        tabbedPane.addTab("Все оценки", createAllGradesPanel());
        tabbedPane.addTab("Оценки по предметам", createSubjectGradesPanel());
        tabbedPane.addTab("Успеваемость", createProgressPanel());
        tabbedPane.addTab("Посещаемость", createAttendancePanel());

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(tabbedPane, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void refreshInfoPanel() {
        infoPanel.removeAll();

        String className = getStudentClass();
        double avgGrade = getAverageGrade();
        int gradesCount = getGradesCount();

        infoPanel.add(createCard("🏫 Класс", className, new Color(52, 152, 219)));
        infoPanel.add(createCard("📊 Средний балл", String.format("%.2f", avgGrade), new Color(46, 204, 113)));
        infoPanel.add(createCard("📝 Всего оценок", String.valueOf(gradesCount), new Color(241, 196, 15)));

        infoPanel.revalidate();
        infoPanel.repaint();
    }

    private JPanel createCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(color, 2), new EmptyBorder(10, 10, 10, 10)));
        card.setPreferredSize(new Dimension(180, 100));

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

    private JPanel createAllGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        String[] columns = {"Предмет", "Оценка", "Комментарий", "Дата"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        loadAllGrades(model);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(950, 300));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSubjectGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        JComboBox<String> subjectCombo = new JComboBox<>();
        subjectCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        subjectCombo.setPreferredSize(new Dimension(200, 30));
        loadSubjects(subjectCombo);
        top.add(new JLabel("Предмет:"));
        top.add(subjectCombo);
        panel.add(top, BorderLayout.NORTH);

        String[] columns = {"Оценка", "Комментарий", "Дата"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        subjectCombo.addActionListener(e -> {
            String subject = (String) subjectCombo.getSelectedItem();
            loadSubjectGrades(model, subject);
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(950, 300));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProgressPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(new EmptyBorder(10, 10, 10, 10));
        container.setBackground(Color.WHITE);

        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        String[] subjects = Database.getSubjects();
        for (String subj : subjects) {
            double avg = getSubjectAverage(subj);
            JProgressBar bar = new JProgressBar(0, 5);
            bar.setValue((int) Math.round(avg));
            bar.setString(subj + ": " + String.format("%.2f", avg));
            bar.setStringPainted(true);
            bar.setForeground(new Color(46, 204, 113));
            bar.setPreferredSize(new Dimension(950, 30));
            panel.add(bar);
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    private JPanel createAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        String[] columns = {"Дата", "Статус"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        loadAttendance(model);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(950, 300));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // Методы для загрузки данных (подключение к Database)
    private void loadSubjects(JComboBox<String> combo) {
        combo.removeAllItems();
        try (ResultSet rs = Database.getAllSubjects()) {
            while (rs != null && rs.next()) {
                combo.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void loadAllGrades(DefaultTableModel model) { Database.loadAllGrades(studentId, model); }
    private void loadSubjectGrades(DefaultTableModel model, String subject) { Database.loadSubjectGrades(studentId, subject, model); }
    private String getStudentClass() { return Database.getStudentClass(studentId); }
    private double getAverageGrade() { return Database.getAverageGrade(studentId); }
    private int getGradesCount() { return Database.getGradesCount(studentId); }
    private double getSubjectAverage(String subject) { return Database.getSubjectAverage(studentId, subject); }
    private void loadAttendance(DefaultTableModel model) { Database.loadAttendance(studentId, model); }
}
