// EditorPanel.java
package com.simplenotes.presentation.views;

import com.simplenotes.domain.entities.Note;
import com.simplenotes.domain.events.CategoryChangePublisher;
import com.simplenotes.infrastructure.persistence.dao.CategoryDao;
import com.simplenotes.infrastructure.persistence.dao.CategoryDao.CategoryDaoException;
import com.simplenotes.infrastructure.persistence.dao.NoteDao;
import com.simplenotes.infrastructure.persistence.dao.NoteDao.NoteDaoException;
import com.simplenotes.infrastructure.services.AutoSaveService;
import com.simplenotes.domain.entities.Category;
import com.simplenotes.domain.events.CategoryChangeListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EditorPanel extends JPanel implements CategoryChangeListener { 
    private JTextArea contentArea;
    private JTextField titleField;
    private JToolBar toolbar;
    private JComboBox<Category> categoryComboBox;
    private NoteDao noteDao;
    private CategoryDao categoryDao;
    private Note currentNote;
    private JPanel categoryPanel;
    private AutoSaveService autoSaveService;

    public EditorPanel() {
        try {
            this.noteDao = new NoteDao();
            this.categoryDao = new CategoryDao();
            this.autoSaveService = new AutoSaveService(noteDao);

            CategoryChangePublisher.getInstance().addListener(this);

            initComponents();
            layoutComponents();
            setupListeners();
            setupAutoSave();
            refreshCategoryComboBox();
            loadLastAutoSavedNote(); // Load last autosaved note on startup
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error initializing database connection: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void onCategoryAdded(Category category) {
        SwingUtilities.invokeLater(() -> {
            categoryComboBox.addItem(category);
        });
    }

    @Override
    public void onCategoryUpdated(Category category) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                Category item = categoryComboBox.getItemAt(i);
                if (item != null && item.getId().equals(category.getId())) {
                    categoryComboBox.removeItemAt(i);
                    categoryComboBox.insertItemAt(category, i);
                    if (categoryComboBox.getSelectedIndex() == i) {
                        categoryComboBox.setSelectedItem(category);
                    }
                    break;
                }
            }
        });
    }

    @Override
    public void onCategoryDeleted(Long categoryId) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                Category item = categoryComboBox.getItemAt(i);
                if (item != null && item.getId().equals(categoryId)) {
                    categoryComboBox.removeItemAt(i);
                    break;
                }
            }
        });
    }
    private void setupAutoSave() {
        // Start autosave service
        autoSaveService.startAutoSave();

        // Add document listeners to track changes
        titleField.getDocument().addDocumentListener(new DocumentListener() {
            public void changed() {
                updateAutoSaveState();
            }
            public void insertUpdate(DocumentEvent e) { changed(); }
            public void removeUpdate(DocumentEvent e) { changed(); }
            public void changedUpdate(DocumentEvent e) { changed(); }
        });

        contentArea.getDocument().addDocumentListener(new DocumentListener() {
            public void changed() {
                updateAutoSaveState();
            }
            public void insertUpdate(DocumentEvent e) { changed(); }
            public void removeUpdate(DocumentEvent e) { changed(); }
            public void changedUpdate(DocumentEvent e) { changed(); }
        });

        // Add window listener to handle application close
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    autoSaveService.stopAutoSave();
                }
            });
        }
    }

    private void updateAutoSaveState() {
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        Long categoryId = selectedCategory != null ? selectedCategory.getId() : null;
        autoSaveService.updateCurrentState(
            titleField.getText(),
            contentArea.getText(),
            categoryId
        );
    }

    private void loadLastAutoSavedNote() {
        Note lastNote = autoSaveService.getLastAutoSavedNote();
        if (lastNote != null) {
            loadNote(lastNote);
        }
    }

    public void loadNote(Note note) {
        currentNote = note;
        autoSaveService.setCurrentNote(note);
        if (note != null) {
            titleField.setText(note.getTitle());
            contentArea.setText(note.getContent());
            // Find and select the correct category in the combo box
            for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                Category category = categoryComboBox.getItemAt(i);
                if (category != null && category.getId().equals(note.getCategoryId())) {
                    categoryComboBox.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            clearFields();
        }
    }

    // Tambahkan method setupListeners
    @SuppressWarnings("unused")
    private void setupListeners() {
        // Listener untuk categoryComboBox
        categoryComboBox.addActionListener(e -> {
            if (categoryComboBox.getSelectedItem() != null) {
                // Handle category selection change if needed
            }
        });

        // Listener untuk titleField jika diperlukan
        titleField.addActionListener(e -> {
            // Handle title field action if needed
        });

        // Listener untuk contentArea jika diperlukan
        contentArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                // Handle content changes when focus is lost if needed
            }
        });
    }

    private void initComponents() {
        titleField = new JTextField();
        titleField.setFont(new Font("Arial", Font.BOLD, 14));
        
        contentArea = new JTextArea();
        contentArea.setFont(new Font("Arial", Font.PLAIN, 12));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        
        categoryComboBox = new JComboBox<>();

        Dimension maxSize = new Dimension(200, 25); // Sesuaikan dengan kebutuhan
        categoryComboBox.setMaximumSize(maxSize);
        categoryComboBox.setPreferredSize(maxSize);

        // Custom renderer dengan ellipsis
        categoryComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                
                if (value == null) {
                    label.setText("-- Select Category --");
                    label.setForeground(Color.GRAY);
                } else if (value instanceof Category) {
                    Category category = (Category) value;
                    // Menggunakan SwingUtilities untuk mengukur text
                    FontMetrics fm = label.getFontMetrics(label.getFont());
                    String text = category.getName();
                    int textWidth = fm.stringWidth(text);
                    int availableWidth = maxSize.width - 20; // Kurangi padding

                    if (textWidth > availableWidth) {
                        // Potong teks dan tambahkan ellipsis
                        StringBuilder sb = new StringBuilder(text);
                        while (fm.stringWidth(sb.toString() + "...") > availableWidth && sb.length() > 0) {
                            sb.setLength(sb.length() - 1);
                        }
                        label.setText(sb + "...");
                        // Set tooltip untuk menampilkan teks lengkap
                        label.setToolTipText(text);
                    } else {
                        label.setText(text);
                        label.setToolTipText(category.getDescription());
                    }
                }
                
                // Tambahkan padding
                label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                
                return label;
            }
        });
        
         // Atur UI khusus untuk popup menu
        categoryComboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected ComboPopup createPopup() {
                BasicComboPopup popup = new BasicComboPopup(comboBox) {
                    @Override
                    protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
                        return super.computePopupBounds(px, py, 
                            Math.max(maxSize.width, pw), // Gunakan lebar maksimum yang lebih besar
                            ph);
                    }
                };
                return popup;
            }
        });
        
        // Tambahkan border merah jika belum dipilih
        categoryComboBox.addActionListener(_ -> {
            if (categoryComboBox.getSelectedItem() == null) {
                categoryComboBox.setBorder(BorderFactory.createLineBorder(Color.RED));
            } else {
                categoryComboBox.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("ComboBox.border"));
            }
        });

        // Tambahkan label yang menunjukkan field wajib
        JLabel requiredLabel = new JLabel("*");
        requiredLabel.setForeground(Color.RED);
        requiredLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Inisialisasi categoryPanel dan simpan ke field class
        categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        categoryPanel.add(new JLabel("Category: "));
        categoryPanel.add(requiredLabel);
        categoryPanel.add(categoryComboBox);

        toolbar = createToolbar();
    }

    private JToolBar createToolbar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        
        JButton newButton = new JButton("New");
        JButton saveButton = new JButton("Save");
        JButton deleteButton = new JButton("Delete");
        
        newButton.addActionListener(this::handleNewNote);
        saveButton.addActionListener(this::handleSaveNote);
        deleteButton.addActionListener(this::handleDeleteNote);
        
        bar.add(newButton);
        bar.add(saveButton);
        bar.add(deleteButton);
        
        return bar;
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(5, 5));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleField, BorderLayout.CENTER);
        
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.add(toolbar, BorderLayout.CENTER);
        toolbarPanel.add(categoryPanel, BorderLayout.EAST); // Sekarang bisa mengakses categoryPanel
        
        topPanel.add(toolbarPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(contentArea), BorderLayout.CENTER);
        
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
    
    
    private void handleNewNote(ActionEvent e) {
        clearFields();
        currentNote = null;
    }

    private void handleDeleteNote(ActionEvent e) {
        if (currentNote != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this note?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    noteDao.delete(currentNote.getId()); // Changed from noteController to noteDao
                    clearFields();
                    currentNote = null;
                    JOptionPane.showMessageDialog(this,
                        "Note deleted successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (NoteDaoException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Error deleting note: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void clearFields() {
        titleField.setText("");
        contentArea.setText("");
        categoryComboBox.setSelectedIndex(-1);
    }

    private void handleSaveNote(ActionEvent e) {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        
        // Validasi judul kosong
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Title cannot be empty",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        // Validasi category harus dipilih
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a category",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            categoryComboBox.requestFocus(); // Set focus ke combobox
            return;
        }
    
        Long categoryId = selectedCategory.getId();
        saveNoteWithCategory(title, content, categoryId);
    }
    
    
    private void refreshCategoryComboBox() {
        categoryComboBox.removeAllItems();
        try {
            List<Category> categories = categoryDao.findAll();
            categoryComboBox.addItem(null); // Option for no category
            categories.forEach(categoryComboBox::addItem);
        } catch (CategoryDaoException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading categories: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveNoteWithCategory(String title, String content, Long categoryId) {
        try {
            if (currentNote == null) {
                // Create new note
                Note newNote = new Note(null, title, content, categoryId);
                currentNote = noteDao.save(newNote);
                JOptionPane.showMessageDialog(this, 
                    "Note created successfully in category", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Update existing note
                currentNote.setTitle(title);
                currentNote.setContent(content);
                currentNote.setCategoryId(categoryId);
                currentNote = noteDao.update(currentNote);
                JOptionPane.showMessageDialog(this, 
                    "Note updated successfully in category", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            // Refresh the view if needed
            refreshCategoryComboBox();
            
        } catch (NoteDaoException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error saving note to category: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // You might also want to add a convenience method to save the current state
    public void saveCurrentNoteToCategory(Long categoryId) {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Title cannot be empty",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        saveNoteWithCategory(title, content, categoryId);
    }

    // Get all notes for a specific category
    public List<Note> getNotesForCategory(Long categoryId) {
        try {
            return noteDao.findByCategoryId(categoryId);
        } catch (NoteDaoException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading notes for category: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }

    // Move a note to a different category
    public void moveNoteToCategory(Long noteId, Long newCategoryId) {
        try {
            Optional<Note> noteOpt = noteDao.findById(noteId);
            if (noteOpt.isPresent()) {
                Note note = noteOpt.get();
                note.setCategoryId(newCategoryId);
                noteDao.update(note);
                JOptionPane.showMessageDialog(this,
                    "Note moved to new category successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NoteDaoException ex) {
            JOptionPane.showMessageDialog(this,
                "Error moving note to category: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

}
