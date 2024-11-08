// MainWindow.java
package com.simplenotes.presentation.views;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private EditorPanel editorPanel;
    private CategoryPanel categoryPanel;
    private final int MIN_WIDTH = 800;  // Minimal width dalam pixel
    private final int MIN_HEIGHT = 600; // Minimal height dalam pixel

    public MainWindow() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initializeComponents();
        setupLayout();
        configureFrame();
    }

    private void initializeComponents() {
        editorPanel = new EditorPanel();
        categoryPanel = new CategoryPanel(editorPanel);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Add category panel to the left
        categoryPanel.setPreferredSize(new Dimension(200, 0));
        add(categoryPanel, BorderLayout.WEST);

        // Add editor panel to the center
        add(editorPanel, BorderLayout.CENTER);

        // Add a small margin around components
        ((JComponent) getContentPane()).setBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private void configureFrame() {
        setTitle("Simple Notes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        // Set minimum size to avoid resizing below this limit
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
    }
}
