// Main.java
package com.simplenotes;

import javax.swing.*;

import com.simplenotes.infrastructure.config.DatabaseConfig;
import com.simplenotes.presentation.views.MainWindow;

public class Main {
    public static void main(String[] args) {
        try {
            // Initialize database
            DatabaseConfig.initializeDatabase();
            
            // Start UI
            SwingUtilities.invokeLater(() -> {
                try {
                    MainWindow window = new MainWindow();
                    window.setVisible(true);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                        "Error starting application: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to initialize application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
