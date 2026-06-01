import java.sql.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:diary.db";
    private static Connection connection = null;
    private static final Logger logger = new Logger();

    public static void initialize() {
        logger.info("Initializing database...");
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            initializeTables();
            insertDefaultData();
            logger.success("The database was initialized successfully");
        } catch (Exception e) {
            logger.error("Database initialization error", e);
        }
    }


    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }


    private static void initializeTables() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, role TEXT NOT NULL, full_name TEXT NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS students (id INTEGER PRIMARY KEY, class_name TEXT NOT NULL, teacher_id INTEGER NOT NULL, FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS subjects (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS grades (id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER NOT NULL, subject_id INTEGER NOT NULL, grade INTEGER CHECK(grade BETWEEN 1 AND 5), date DATE DEFAULT CURRENT_DATE, comment TEXT, FOREIGN KEY (student_id) REFERENCES users(id), FOREIGN KEY (subject_id) REFERENCES subjects(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS teacher_subjects (teacher_id INTEGER NOT NULL, subject_id INTEGER NOT NULL, PRIMARY KEY (teacher_id, subject_id), FOREIGN KEY (teacher_id) REFERENCES users(id), FOREIGN KEY (subject_id) REFERENCES subjects(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS attendance (id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER NOT NULL, date DATE DEFAULT CURRENT_DATE, status TEXT CHECK(status IN ('present','absent','late')), FOREIGN KEY (student_id) REFERENCES users(id))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertDefaultData() {
        try {
            // Проверяем, есть ли уже директор
            PreparedStatement checkStmt = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE role = 'director'");
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                // Добавляем директора
                PreparedStatement pstmt = connection.prepareStatement("INSERT INTO users (username, password, role, full_name) VALUES (?, ?, ?, ?)");
                pstmt.setString(1, "director");
                pstmt.setString(2, "director123");
                pstmt.setString(3, "director");
                pstmt.setString(4, "Директор школы");
                pstmt.executeUpdate();

                // Добавляем предметы
                String[] subjects = {"Математика", "Русский язык", "Литература", "Физика", "Химия", "Биология", "История", "Английский язык"};
                for (String subject : subjects) {
                    try {
                        pstmt = connection.prepareStatement("INSERT OR IGNORE INTO subjects (name) VALUES (?)");
                        pstmt.setString(1, subject);
                        pstmt.executeUpdate();
                    } catch (SQLException e) {
                        // Предмет уже существует
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- Login ---
    public static int[] login(String username, String password) {
        String query = "SELECT id, role FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int roleCode = switch (rs.getString("role")) {
                    case "director" -> 1;
                    case "teacher" -> 2;
                    case "student" -> 3;
                    default -> 0;
                };
                return new int[]{rs.getInt("id"), roleCode};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- Registration ---
    public static boolean registerStudent(String username, String password, String fullName, String className, int teacherId) {
        if (connection == null) return false;
        try {
            String checkUser = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(checkUser)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) return false;
            }

            connection.setAutoCommit(false);
            String insertUser = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, 'student', ?)";
            int studentId = -1;
            try (PreparedStatement pstmt = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, fullName);
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) studentId = rs.getInt(1);
            }

            if (studentId != -1) {
                String insertStudent = "INSERT INTO students (id, class_name, teacher_id) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(insertStudent)) {
                    pstmt.setInt(1, studentId);
                    pstmt.setString(2, className);
                    pstmt.setInt(3, teacherId);
                    pstmt.executeUpdate();
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException ex) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (connection != null) connection.setAutoCommit(true);
            } catch (SQLException e) {
            }
        }
    }

    public static boolean registerTeacher(String username, String password, String fullName, String subject) {
        if (connection == null) return false;
        try {
            String checkUser = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(checkUser)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) return false;
            }

            connection.setAutoCommit(false);
            String insertUser = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, 'teacher', ?)";
            int teacherId = -1;
            try (PreparedStatement pstmt = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, fullName);
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) teacherId = rs.getInt(1);
            }

            if (teacherId != -1 && subject != null && !subject.isEmpty()) {
                String assignSubject = "INSERT INTO teacher_subjects (teacher_id, subject_id) VALUES (?, (SELECT id FROM subjects WHERE name = ?))";
                try (PreparedStatement pstmt = connection.prepareStatement(assignSubject)) {
                    pstmt.setInt(1, teacherId);
                    pstmt.setString(2, subject);
                    pstmt.executeUpdate();
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException ex) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (connection != null) connection.setAutoCommit(true);
            } catch (SQLException e) {
            }
        }
    }

    // --- DirectorUI methods ---
    public static void loadAllTeachers(DefaultTableModel model) {
        model.setRowCount(0);
        String query = "SELECT u.id, u.full_name, s.name as subject FROM users u " +
                "LEFT JOIN teacher_subjects ts ON u.id = ts.teacher_id " +
                "LEFT JOIN subjects s ON ts.subject_id = s.id " +
                "WHERE u.role = 'teacher'";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("subject") != null ? rs.getString("subject") : "Не назначен"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadAllStudents(DefaultTableModel model) {
        model.setRowCount(0);
        String query = "SELECT u.id, u.full_name, s.class_name FROM users u " +
                "JOIN students s ON u.id = s.id WHERE u.role = 'student'";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("class_name")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- TeacherUI methods ---
    public static void loadStudentsForTeacher(int teacherId, DefaultTableModel model) {
        model.setRowCount(0);
        String query = "SELECT u.id, u.full_name, s.class_name, " +
                "(SELECT AVG(grade) FROM grades WHERE student_id = u.id) as avg_grade " +
                "FROM users u JOIN students s ON u.id = s.id " +
                "WHERE u.role = 'student' AND s.teacher_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                double avg = rs.getDouble("avg_grade");
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("class_name"),
                        avg > 0 ? String.format("%.2f", avg) : "Нет оценок"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getStudentCountForTeacher(int teacherId) {
        String query = "SELECT COUNT(*) FROM students WHERE teacher_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double getAverageGradeForTeacher(int teacherId) {
        String query = "SELECT AVG(g.grade) FROM grades g " +
                "JOIN students s ON g.student_id = s.id " +
                "WHERE s.teacher_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static int getTotalGradesForTeacher(int teacherId) {
        String query = "SELECT COUNT(*) FROM grades g " +
                "JOIN students s ON g.student_id = s.id " +
                "WHERE s.teacher_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // --- StudentUI methods ---
    public static String[] getSubjects() {
        ArrayList<String> subjects = new ArrayList<>();
        String query = "SELECT name FROM subjects ORDER BY name";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) subjects.add(rs.getString("name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subjects.toArray(new String[0]);
    }

    public static ResultSet getAllSubjects() {
        String query = "SELECT name FROM subjects ORDER BY name";
        try {
            Statement stmt = getConnection().createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void loadAllGrades(int studentId, DefaultTableModel model) {
        model.setRowCount(0);
        String query = "SELECT s.name, g.grade, g.comment, g.date FROM grades g " +
                "JOIN subjects s ON g.subject_id = s.id " +
                "WHERE g.student_id = ? ORDER BY g.date DESC";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getInt("grade"),
                        rs.getString("comment") != null ? rs.getString("comment") : "",
                        rs.getString("date")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadSubjectGrades(int studentId, String subject, DefaultTableModel model) {
        model.setRowCount(0);
        String query = "SELECT g.grade, g.comment, g.date FROM grades g " +
                "JOIN subjects s ON g.subject_id = s.id " +
                "WHERE g.student_id = ? AND s.name = ? ORDER BY g.date DESC";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, subject);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("grade"),
                        rs.getString("comment") != null ? rs.getString("comment") : "",
                        rs.getString("date")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadAttendance(int studentId, DefaultTableModel model) {
        model.setRowCount(0);
        String query = "SELECT date, status FROM attendance WHERE student_id = ? ORDER BY date DESC LIMIT 20";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String status = switch (rs.getString("status")) {
                    case "present" -> "✅ Присутствовал";
                    case "absent" -> "❌ Отсутствовал";
                    case "late" -> "⏰ Опоздал";
                    default -> rs.getString("status");
                };
                model.addRow(new Object[]{rs.getString("date"), status});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getStudentClass(int studentId) {
        String result = "Не указан";
        String query = "SELECT class_name FROM students WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) result = rs.getString("class_name");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static double getAverageGrade(int studentId) {
        double result = 0.0;
        String query = "SELECT AVG(grade) AS avg_grade FROM grades WHERE student_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) result = rs.getDouble("avg_grade");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int getGradesCount(int studentId) {
        int result = 0;
        String query = "SELECT COUNT(*) AS count FROM grades WHERE student_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) result = rs.getInt("count");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static double getSubjectAverage(int studentId, String subject) {
        double result = 0.0;
        String query = "SELECT AVG(g.grade) AS avg_grade FROM grades g " +
                "JOIN subjects s ON g.subject_id = s.id " +
                "WHERE g.student_id = ? AND s.name = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, subject);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) result = rs.getDouble("avg_grade");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean deleteTeacher(int teacherId) {
        if (connection == null) return false;
        try {
            connection.setAutoCommit(false);

            // Сначала удаляем связи учителя с предметами
            String deleteTeacherSubjects = "DELETE FROM teacher_subjects WHERE teacher_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteTeacherSubjects)) {
                pstmt.setInt(1, teacherId);
                pstmt.executeUpdate();
            }

            // Получаем всех учеников этого учителя
            String getStudents = "SELECT id FROM students WHERE teacher_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(getStudents)) {
                pstmt.setInt(1, teacherId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int studentId = rs.getInt("id");
                    // Удаляем оценки учеников
                    try (PreparedStatement pstmt2 = connection.prepareStatement("DELETE FROM grades WHERE student_id = ?")) {
                        pstmt2.setInt(1, studentId);
                        pstmt2.executeUpdate();
                    }
                    // Удаляем посещаемость учеников
                    try (PreparedStatement pstmt2 = connection.prepareStatement("DELETE FROM attendance WHERE student_id = ?")) {
                        pstmt2.setInt(1, studentId);
                        pstmt2.executeUpdate();
                    }
                    // Удаляем запись ученика в students
                    try (PreparedStatement pstmt2 = connection.prepareStatement("DELETE FROM students WHERE id = ?")) {
                        pstmt2.setInt(1, studentId);
                        pstmt2.executeUpdate();
                    }
                    // Удаляем пользователя-ученика
                    try (PreparedStatement pstmt2 = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
                        pstmt2.setInt(1, studentId);
                        pstmt2.executeUpdate();
                    }
                }
            }

            // Удаляем учителя
            String deleteTeacher = "DELETE FROM users WHERE id = ? AND role = 'teacher'";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteTeacher)) {
                pstmt.setInt(1, teacherId);
                pstmt.executeUpdate();
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException ex) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (connection != null) connection.setAutoCommit(true);
            } catch (SQLException e) {
            }
        }
    }

    // --- Удаление ученика (для учителя) ---
    public static boolean deleteStudent(int studentId) {
        if (connection == null) return false;
        try {
            connection.setAutoCommit(false);

            // Удаляем оценки ученика
            String deleteGrades = "DELETE FROM grades WHERE student_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteGrades)) {
                pstmt.setInt(1, studentId);
                pstmt.executeUpdate();
            }

            // Удаляем посещаемость ученика
            String deleteAttendance = "DELETE FROM attendance WHERE student_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteAttendance)) {
                pstmt.setInt(1, studentId);
                pstmt.executeUpdate();
            }

            // Удаляем запись ученика в students
            String deleteStudent = "DELETE FROM students WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteStudent)) {
                pstmt.setInt(1, studentId);
                pstmt.executeUpdate();
            }

            // Удаляем пользователя
            String deleteUser = "DELETE FROM users WHERE id = ? AND role = 'student'";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteUser)) {
                pstmt.setInt(1, studentId);
                pstmt.executeUpdate();
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException ex) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (connection != null) connection.setAutoCommit(true);
            } catch (SQLException e) {
            }
        }
    }

    // --- Получение списка учеников учителя (для посещаемости) ---
    public static ResultSet getStudentsForAttendance(int teacherId) {
        String query = "SELECT u.id, u.full_name, s.class_name FROM users u " +
                "JOIN students s ON u.id = s.id " +
                "WHERE u.role = 'student' AND s.teacher_id = ? " +
                "ORDER BY u.full_name";
        try {
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            pstmt.setInt(1, teacherId);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- Добавление/обновление посещаемости ---
    public static boolean saveAttendance(int studentId, String date, String status) {
        if (connection == null) return false;
        try {
            String query = "INSERT OR REPLACE INTO attendance (student_id, date, status) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setInt(1, studentId);
                pstmt.setString(2, date);
                pstmt.setString(3, status);
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Получение посещаемости для конкретного ученика ---
    public static String getAttendanceForDate(int studentId, String date) {
        String query = "SELECT status FROM attendance WHERE student_id = ? AND date = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, date);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- Получение предметов учителя ---
    public static ResultSet getTeacherSubjects(int teacherId) {
        String query = "SELECT s.name FROM subjects s " +
                "JOIN teacher_subjects ts ON s.id = ts.subject_id " +
                "WHERE ts.teacher_id = ?";
        try {
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            pstmt.setInt(1, teacherId);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- Добавление оценки ---
    public static boolean addGrade(int studentId, int teacherId, String subject, int grade, String comment) {
        if (connection == null) return false;
        try {
            String query = "INSERT INTO grades (student_id, subject_id, grade, date, comment) " +
                    "VALUES (?, (SELECT id FROM subjects WHERE name = ?), ?, date('now'), ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setInt(1, studentId);
                pstmt.setString(2, subject);
                pstmt.setInt(3, grade);
                pstmt.setString(4, comment);
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}