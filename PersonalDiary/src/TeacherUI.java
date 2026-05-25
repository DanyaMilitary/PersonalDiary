import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TeacherUI extends JFrame {
    private int teacherId;
    private JTabbedPane tabbedPane;
    private JPanel infoPanel;

    public TeacherUI(int teacherId) {
        this.teacherId = teacherId;

        setTitle("Личный кабинет учителя");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(245, 245, 250));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Верхняя панель
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(41, 128, 185));
        topPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("👨‍🏫 Личный кабинет учителя");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.BLACK);
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

        // Панель информации и кнопок действий
        infoPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        infoPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        infoPanel.setBackground(new Color(245, 245, 250));
        refreshInfoPanel();

        // Вкладки
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 13));
        tabbedPane.addTab("Мои ученики", createStudentsPanel());
        tabbedPane.addTab("Добавить ученика", createAddStudentPanel());
        tabbedPane.addTab("Выставить оценки", createGradePanel());
        tabbedPane.addTab("Посещаемость", createAttendanceManagementPanel());

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(tabbedPane, BorderLayout.SOUTH);

        add(mainPanel);
    }

    public void refreshInfoPanel() {
        infoPanel.removeAll();
        infoPanel.add(createCard("👥 Кол-во учеников", String.valueOf(getStudentCount()), new Color(52, 152, 219)));
        infoPanel.add(createCard("📊 Средний балл", String.format("%.2f", getAverageGrade()), new Color(46, 204, 113)));
        infoPanel.add(createCard("📝 Всего оценок", String.valueOf(getTotalGrades()), new Color(241, 196, 15)));
        infoPanel.revalidate();
        infoPanel.repaint();
    }

    private JPanel createCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(color, 2), new EmptyBorder(10, 10, 10, 10)));
        card.setPreferredSize(new Dimension(180, 800));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(color);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 14));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"ID", "Имя", "Класс", "Средний балл", "Действия"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return col == 4; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));

        // Добавляем кнопку удаления в таблицу
        table.getColumn("Действия").setCellRenderer(new ButtonRenderer());
        table.getColumn("Действия").setCellEditor(new ButtonEditor(new JCheckBox(), table, model, this));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(950, 400));
        panel.add(scrollPane, BorderLayout.CENTER);

        loadStudents(model);
        return panel;
    }

    private JPanel createAddStudentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel nameLabel = new JLabel("Имя ученика:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(nameLabel, gbc);

        JTextField nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        JLabel loginLabel = new JLabel("Логин:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(loginLabel, gbc);

        JTextField loginField = new JTextField();
        loginField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        panel.add(loginField, gbc);

        JLabel passLabel = new JLabel("Пароль:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passLabel, gbc);

        JPasswordField passField = new JPasswordField();
        passField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        panel.add(passField, gbc);

        JLabel classLabel = new JLabel("Класс:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(classLabel, gbc);

        JTextField classField = new JTextField();
        classField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        panel.add(classField, gbc);

        JButton addBtn = new JButton("Добавить ученика");
        addBtn.setBackground(new Color(46, 204, 113));
        addBtn.setForeground(Color.BLACK);
        addBtn.setFont(new Font("Arial", Font.BOLD, 14));
        addBtn.setPreferredSize(new Dimension(200, 35));
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(addBtn, gbc);

        addBtn.addActionListener(e -> {
            String name = nameField.getText();
            String login = loginField.getText();
            String password = new String(passField.getPassword());
            String clazz = classField.getText();

            if (name.isEmpty() || login.isEmpty() || password.isEmpty() || clazz.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Заполните все поля!");
                return;
            }

            if (Database.registerStudent(login, password, name, clazz, teacherId)) {
                JOptionPane.showMessageDialog(this, "Ученик успешно добавлен!");
                nameField.setText("");
                loginField.setText("");
                passField.setText("");
                classField.setText("");
                refreshInfoPanel();
                refreshStudentsTable();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении ученика.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createGradePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Выбор ученика
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Ученик:"), gbc);

        JComboBox<String> studentCombo = new JComboBox<>();
        studentCombo.setPreferredSize(new Dimension(250, 30));
        loadStudentsForCombo(studentCombo);
        gbc.gridx = 1;
        panel.add(studentCombo, gbc);

        // Выбор предмета
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Предмет:"), gbc);

        JComboBox<String> subjectCombo = new JComboBox<>();
        subjectCombo.setPreferredSize(new Dimension(250, 30));
        loadTeacherSubjects(subjectCombo);
        gbc.gridx = 1;
        panel.add(subjectCombo, gbc);

        // Оценка
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Оценка (1-5):"), gbc);

        JComboBox<Integer> gradeCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        gradeCombo.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        panel.add(gradeCombo, gbc);

        // Комментарий
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Комментарий:"), gbc);

        JTextArea commentArea = new JTextArea(3, 20);
        JScrollPane commentScroll = new JScrollPane(commentArea);
        commentScroll.setPreferredSize(new Dimension(250, 60));
        gbc.gridx = 1;
        panel.add(commentScroll, gbc);

        // Кнопка сохранения
        JButton saveBtn = new JButton("Сохранить оценку");
        saveBtn.setBackground(new Color(46, 204, 113));
        saveBtn.setForeground(Color.BLACK);
        saveBtn.setFont(new Font("Arial", Font.BOLD, 14));
        saveBtn.setPreferredSize(new Dimension(250, 35));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            if (studentCombo.getSelectedItem() == null || subjectCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Выберите ученика и предмет");
                return;
            }

            String studentInfo = (String) studentCombo.getSelectedItem();
            int studentId = Integer.parseInt(studentInfo.split(" - ")[0]);
            String subject = (String) subjectCombo.getSelectedItem();
            int grade = (Integer) gradeCombo.getSelectedItem();
            String comment = commentArea.getText();

            if (Database.addGrade(studentId, teacherId, subject, grade, comment)) {
                JOptionPane.showMessageDialog(this, "Оценка успешно выставлена!");
                commentArea.setText("");
                refreshInfoPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при выставлении оценки", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createAttendanceManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Верхняя панель с выбором даты
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(Color.WHITE);

        JLabel dateLabel = new JLabel("Дата:");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 13));
        topPanel.add(dateLabel);

        JTextField dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        dateField.setPreferredSize(new Dimension(120, 30));
        topPanel.add(dateField);

        JButton loadBtn = new JButton("Загрузить");
        loadBtn.setBackground(new Color(52, 152, 219));
        loadBtn.setForeground(Color.BLACK);
        loadBtn.setFocusPainted(false);
        topPanel.add(loadBtn);

        JButton saveAllBtn = new JButton("Сохранить всё");
        saveAllBtn.setBackground(new Color(46, 204, 113));
        saveAllBtn.setForeground(Color.BLACK);
        saveAllBtn.setFocusPainted(false);
        topPanel.add(saveAllBtn);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Таблица учеников
        String[] columns = {"ID", "Ученик", "Класс", "Посещаемость"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return col == 3; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        // Настройка комбобокса для посещаемости
        TableColumn statusColumn = table.getColumnModel().getColumn(3);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"", "present", "absent", "late"});
        statusCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    String status = value.toString();
                    switch (status) {
                        case "present" -> setText("✅ Присутствовал");
                        case "absent" -> setText("❌ Отсутствовал");
                        case "late" -> setText("⏰ Опоздал");
                        case "" -> setText("Не указано");
                        default -> setText(status);
                    }
                }
                return this;
            }
        });
        statusColumn.setCellEditor(new DefaultCellEditor(statusCombo));
        statusColumn.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value != null) {
                    String status = value.toString();
                    switch (status) {
                        case "present" -> setText("✅ Присутствовал");
                        case "absent" -> setText("❌ Отсутствовал");
                        case "late" -> setText("⏰ Опоздал");
                        case "" -> setText("Не указано");
                        default -> setText(status);
                    }
                } else {
                    setText("");
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(950, 400));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Загрузка учеников
        java.util.List<Integer> studentIds = new java.util.ArrayList<>();

        Runnable loadStudents = () -> {
            model.setRowCount(0);
            studentIds.clear();
            try (ResultSet rs = Database.getStudentsForAttendance(teacherId)) {
                while (rs != null && rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("full_name");
                    String className = rs.getString("class_name");
                    studentIds.add(id);

                    String currentStatus = Database.getAttendanceForDate(id, dateField.getText());
                    String statusDisplay = currentStatus != null ? currentStatus : "";

                    model.addRow(new Object[]{id, name, className, statusDisplay});
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };

        loadBtn.addActionListener(e -> loadStudents.run());
        loadStudents.run();

        saveAllBtn.addActionListener(e -> {
            String date = dateField.getText();
            if (date.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите дату!");
                return;
            }

            boolean allSuccess = true;

            for (int i = 0; i < model.getRowCount(); i++) {
                int studentIdValue = (int) model.getValueAt(i, 0);
                String status = (String) model.getValueAt(i, 3);
                if (status != null && !status.isEmpty()) {
                    if (!Database.saveAttendance(studentIdValue, date, status)) {
                        allSuccess = false;
                    }
                }
            }

            if (allSuccess) {
                JOptionPane.showMessageDialog(this, "Посещаемость сохранена!");
                refreshInfoPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при сохранении посещаемости", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        return mainPanel;
    }

    // Вспомогательные методы
    private void loadStudents(DefaultTableModel model) {
        Database.loadStudentsForTeacher(teacherId, model);
    }

    private void loadStudentsForCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        try (ResultSet rs = Database.getStudentsForAttendance(teacherId)) {
            while (rs != null && rs.next()) {
                combo.addItem(rs.getInt("id") + " - " + rs.getString("full_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTeacherSubjects(JComboBox<String> combo) {
        combo.removeAllItems();
        try (ResultSet rs = Database.getTeacherSubjects(teacherId)) {
            while (rs != null && rs.next()) {
                combo.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshStudentsTable() {
        Component component = tabbedPane.getComponentAt(0);
        if (component instanceof JPanel) {
            JPanel panel = (JPanel) component;
            Component[] components = panel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) comp;
                    JViewport viewport = scrollPane.getViewport();
                    Component view = viewport.getView();
                    if (view instanceof JTable) {
                        JTable table = (JTable) view;
                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                        loadStudents(model);
                        break;
                    }
                }
            }
        }
    }

    private int getStudentCount() {
        return Database.getStudentCountForTeacher(teacherId);
    }

    private double getAverageGrade() {
        return Database.getAverageGradeForTeacher(teacherId);
    }

    private int getTotalGrades() {
        return Database.getTotalGradesForTeacher(teacherId);
    }

    // Класс для рендеринга кнопок
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton button;

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER));
            button = new JButton("Удалить");
            button.setBackground(new Color(231, 76, 60));
            button.setForeground(Color.BLACK);
            button.setFocusPainted(false);
            add(button);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Класс для редактора кнопок
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int row;
        private JTable table;
        private DefaultTableModel model;
        private TeacherUI parent;

        public ButtonEditor(JCheckBox checkBox, JTable table, DefaultTableModel model, TeacherUI parent) {
            super(checkBox);
            this.table = table;
            this.model = model;
            this.parent = parent;
            button = new JButton("Удалить");
            button.setBackground(new Color(231, 76, 60));
            button.setForeground(Color.BLACK);
            button.setFocusPainted(false);
            button.addActionListener(e -> {
                int id = (int) model.getValueAt(row, 0);
                String name = (String) model.getValueAt(row, 1);

                int confirm = JOptionPane.showConfirmDialog(parent,
                        "Вы уверены, что хотите удалить ученика " + name + "?\n" +
                                "Все оценки и данные о посещаемости будут также удалены!",
                        "Подтверждение удаления", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = Database.deleteStudent(id);

                    if (success) {
                        JOptionPane.showMessageDialog(parent, "Ученик успешно удален!");
                        model.removeRow(row);
                        parent.refreshInfoPanel();
                    } else {
                        JOptionPane.showMessageDialog(parent,
                                "Ошибка при удалении", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.row = row;
            return button;
        }
    }
}