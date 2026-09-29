package com.smartattendance;

import org.opencv.core.Core;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Database.initializeDatabase();

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
