package com.smartattendance;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.util.List;

public class MainFrame extends JFrame {
    private final FaceRecognizer recognizer = new FaceRecognizer();
    private final JComboBox<Student> studentBox = new JComboBox<>();
    private final JLabel status = new JLabel("Ready");

    public MainFrame() {
        setTitle("Smart Attendance System");
        setSize(850, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildUI();
        refreshStudents();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Smart Attendance Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        root.add(title, BorderLayout.NORTH);

        JPanel controls = new JPanel(new GridLayout(0, 2, 10, 10));

        JButton add = new JButton("Add Student");
        JButton capture = new JButton("Capture Face");
        JButton train = new JButton("Train Model");
        JButton start = new JButton("Start Attendance");
        JButton today = new JButton("Today's Attendance");
        JButton export = new JButton("Export CSV");
        JButton stats = new JButton("Attendance Percentage");
        JButton refresh = new JButton("Refresh Students");

        controls.add(new JLabel("Select Student:"));
        controls.add(studentBox);
        controls.add(add);
        controls.add(capture);
        controls.add(train);
        controls.add(start);
        controls.add(today);
        controls.add(export);
        controls.add(stats);
        controls.add(refresh);

        root.add(controls, BorderLayout.CENTER);

        status.setBorder(BorderFactory.createTitledBorder("Status"));
        root.add(status, BorderLayout.SOUTH);

        add(root);

        add.addActionListener(e -> addStudent());
        capture.addActionListener(e -> captureFace());
        train.addActionListener(e -> train());
        start.addActionListener(e -> startAttendance());
        today.addActionListener(e -> showToday());
        export.addActionListener(e -> exportCsv());
        stats.addActionListener(e -> showStats());
        refresh.addActionListener(e -> refreshStudents());
    }

    private void addStudent() {
        JTextField name = new JTextField();
        JTextField roll = new JTextField();

        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.add(new JLabel("Name:"));
        p.add(name);
        p.add(new JLabel("Roll Number:"));
        p.add(roll);

        int result = JOptionPane.showConfirmDialog(
            this, p, "Add Student", JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        if (name.getText().isBlank() || roll.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter both fields.");
            return;
        }

        try {
            int id = Database.addStudent(name.getText().trim(), roll.getText().trim());
            status.setText("Student added. ID = " + id);
            refreshStudents();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this, "Could not add student: " + ex.getMessage()
            );
        }
    }

    private void captureFace() {
        Student s = (Student) studentBox.getSelectedItem();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Select a student first.");
            return;
        }

        new Thread(() -> {
            try {
                FaceCapture capture = new FaceCapture();
                int count = capture.capture(s.getId(), 30);
                SwingUtilities.invokeLater(() ->
                    status.setText("Captured " + count + " face images for " + s.getName())
                );
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, ex.getMessage())
                );
            }
        }).start();
    }

    private void train() {
        new Thread(() -> {
            try {
                recognizer.train();
                SwingUtilities.invokeLater(() -> status.setText("Face model trained successfully."));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, ex.getMessage())
                );
            }
        }).start();
    }

    private void startAttendance() {
        new Thread(() -> {
            try {
                new AttendanceSystem(recognizer).start();
                SwingUtilities.invokeLater(() -> status.setText("Attendance session ended."));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, ex.getMessage())
                );
            }
        }).start();
    }

    private void showToday() {
        try {
            List<String[]> rows = Database.getTodayAttendance();
            String[] cols = {"Name", "Roll Number", "Date", "Time"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);

            for (String[] row : rows) model.addRow(row);

            JTable table = new JTable(model);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(700, 350));

            JOptionPane.showMessageDialog(
                this, scroll, "Today's Attendance",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void exportCsv() {
        try {
            List<String[]> rows = Database.getTodayAttendance();

            try (FileWriter out = new FileWriter("attendance_" +
                    java.time.LocalDate.now() + ".csv")) {
                out.write("Name,Roll Number,Date,Time\n");
                for (String[] row : rows) {
                    out.write(csv(row[0]) + "," + csv(row[1]) + "," +
                              csv(row[2]) + "," + csv(row[3]) + "\n");
                }
            }

            status.setText("CSV exported successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void showStats() {
        Student s = (Student) studentBox.getSelectedItem();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Select a student first.");
            return;
        }

        try {
            int present = Database.getPresentDays(s.getId());
            int totalDays = Database.getTotalMarkedDays();
            double percentage = totalDays == 0 ? 0 : (present * 100.0 / totalDays);

            JOptionPane.showMessageDialog(
                this,
                s.getName() + "\nPresent Days: " + present +
                "\nMarked Working Days: " + totalDays +
                String.format("\nAttendance: %.2f%%", percentage),
                "Attendance Percentage",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void refreshStudents() {
        try {
            studentBox.removeAllItems();
            for (Student s : Database.getStudents()) studentBox.addItem(s);
            status.setText("Student list refreshed.");
        } catch (Exception ex) {
            status.setText("Could not load students.");
        }
    }
}
