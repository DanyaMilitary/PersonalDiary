import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DirectorUI extends JFrame {
    private int directorId;
    private JTabbedPane tabbedPane;

    public DirectorUI(int directorId) {
        this.directorId = directorId;

        setTitle("Личный кабинет директора");
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

        JLabel titleLabel = new JLabel("🎓 Кабинет директора");
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

        // Вкладки
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 13));
        tabbedPane.addTab("Учителя", createTeachersPanel());
        tabbedPane.addTab("Ученики", createStudentsPanel());
        tabbedPane.addTab("Добавить учителя", createAddTeacherPanel());

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createTeachersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"ID", "Имя", "Предмет", "Действия"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return col == 3; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        // Добавляем кнопку удаления в таблицу
        table.getColumn("Действия").setCellRenderer(new ButtonRenderer());
        table.getColumn("Действия").setCellEditor(new ButtonEditor(new JCheckBox(), table, model, this, true));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(950, 400));
        panel.add(scrollPane, BorderLayout.CENTER);

        Database.loadAllTeachers(model);
        return panel;
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"ID", "Имя", "Класс"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(950, 400));
        panel.add(scrollPane, BorderLayout.CENTER);

        Database.loadAllStudents(model);
        return panel;
    }

    private JPanel createAddTeacherPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel nameLabel = new JLabel("Имя учителя:");
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

        JLabel subjectLabel = new JLabel("Предмет:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(subjectLabel, gbc);

        JComboBox<String> subjectCombo = new JComboBox<>();
        subjectCombo.setPreferredSize(new Dimension(200, 30));
        loadSubjects(subjectCombo);
        gbc.gridx = 1;
        panel.add(subjectCombo, gbc);

        JButton addBtn = new JButton("Добавить учителя");
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
            String subject = (String) subjectCombo.getSelectedItem();

            if (name.isEmpty() || login.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Заполните все поля!");
                return;
            }

            if (Database.registerTeacher(login, password, name, subject)) {
                JOptionPane.showMessageDialog(this, "Учитель успешно добавлен!");
                nameField.setText("");
                loginField.setText("");
                passField.setText("");
                // Обновляем таблицу учителей
                refreshTeachersTable();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении учителя.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private void loadSubjects(JComboBox<String> combo) {
        combo.removeAllItems();
        String[] subjects = Database.getSubjects();
        for (String subject : subjects) {
            combo.addItem(subject);
        }
    }

    private void refreshTeachersTable() {
        // Обновляем первую вкладку
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
                        Database.loadAllTeachers(model);
                        break;
                    }
                }
            }
        }
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
        private JFrame parent;
        private boolean isTeacher;

        public ButtonEditor(JCheckBox checkBox, JTable table, DefaultTableModel model, JFrame parent, boolean isTeacher) {
            super(checkBox);
            this.table = table;
            this.model = model;
            this.parent = parent;
            this.isTeacher = isTeacher;
            button = new JButton("Удалить");
            button.setBackground(new Color(231, 76, 60));
            button.setForeground(Color.BLACK);
            button.setFocusPainted(false);
            button.addActionListener(e -> {
                int id = (int) model.getValueAt(row, 0);
                String name = (String) model.getValueAt(row, 1);

                int confirm = JOptionPane.showConfirmDialog(parent,
                        "Вы уверены, что хотите удалить " + (isTeacher ? "учителя" : "ученика") + " " + name + "?\n" +
                                "Все связанные данные будут также удалены!",
                        "Подтверждение удаления", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = false;
                    if (isTeacher) {
                        success = Database.deleteTeacher(id);
                    } else {
                        success = Database.deleteStudent(id);
                    }

                    if (success) {
                        JOptionPane.showMessageDialog(parent,
                                (isTeacher ? "Учитель" : "Ученик") + " успешно удален!");
                        model.removeRow(row);
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