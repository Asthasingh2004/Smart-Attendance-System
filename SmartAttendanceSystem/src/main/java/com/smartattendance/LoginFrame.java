package com.smartattendance;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    public LoginFrame() {
        setTitle("Smart Attendance - Admin Login");
        setSize(420, 280);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("SMART ATTENDANCE SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();
        JButton login = new JButton("Login");

        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        panel.add(title, g);
        g.gridwidth = 1;

        g.gridx = 0; g.gridy++;
        panel.add(new JLabel("Username:"), g);
        g.gridx = 1;
        panel.add(user, g);

        g.gridx = 0; g.gridy++;
        panel.add(new JLabel("Password:"), g);
        g.gridx = 1;
        panel.add(pass, g);

        g.gridx = 0; g.gridy++;
        g.gridwidth = 2;
        panel.add(login, g);

        add(panel);

        login.addActionListener(e -> {
            String u = user.getText().trim();
            String p = new String(pass.getPassword());

            if ("admin".equals(u) && "admin123".equals(p)) {
                dispose();
                new MainFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(
                    this, "Invalid username or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
