package com.smartattendance;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class Database {
    private static final String URL = "jdbc:sqlite:attendance.db";

    private Database() {}

    public static void initializeDatabase() {
        String students = """
            CREATE TABLE IF NOT EXISTS students(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                roll_number TEXT NOT NULL UNIQUE
            )
            """;

        String attendance = """
            CREATE TABLE IF NOT EXISTS attendance(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                time TEXT NOT NULL,
                UNIQUE(student_id, date),
                FOREIGN KEY(student_id) REFERENCES students(id)
            )
            """;

        try (Connection c = DriverManager.getConnection(URL);
             Statement s = c.createStatement()) {
            s.execute(students);
            s.execute(attendance);
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize database", e);
        }
    }

    public static int addStudent(String name, String rollNumber) throws SQLException {
        String sql = "INSERT INTO students(name, roll_number) VALUES(?, ?)";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, name);
            p.setString(2, rollNumber);
            p.executeUpdate();
            try (ResultSet rs = p.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public static List<Student> getStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT id, name, roll_number FROM students ORDER BY id";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(new Student(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("roll_number")
                ));
            }
        }
        return list;
    }

    public static Student getStudent(int id) throws SQLException {
        String sql = "SELECT id, name, roll_number FROM students WHERE id=?";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next()
                    ? new Student(rs.getInt(1), rs.getString(2), rs.getString(3))
                    : null;
            }
        }
    }

    public static boolean markAttendance(int studentId) throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO attendance(student_id, date, time)
            VALUES(?, ?, ?)
            """;
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, studentId);
            p.setString(2, LocalDate.now().toString());
            p.setString(3, LocalTime.now().withNano(0).toString());
            return p.executeUpdate() == 1;
        }
    }

    public static List<String[]> getTodayAttendance() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        String sql = """
            SELECT s.name, s.roll_number, a.date, a.time
            FROM attendance a
            JOIN students s ON s.id=a.student_id
            WHERE a.date=?
            ORDER BY a.time
            """;
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, LocalDate.now().toString());
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                        rs.getString(1), rs.getString(2),
                        rs.getString(3), rs.getString(4)
                    });
                }
            }
        }
        return rows;
    }

    public static boolean isPresentToday(int studentId) throws SQLException {
        String sql = "SELECT 1 FROM attendance WHERE student_id=? AND date=?";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, studentId);
            p.setString(2, LocalDate.now().toString());
            try (ResultSet rs = p.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static int getPresentDays(int studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE student_id=?";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, studentId);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public static int getTotalMarkedDays() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT date) FROM attendance";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
