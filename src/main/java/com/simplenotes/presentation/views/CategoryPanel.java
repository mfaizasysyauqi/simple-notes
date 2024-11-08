// CategoryPanel.java
package com.simplenotes.presentation.views;

import com.simplenotes.domain.entities.Category;
import com.simplenotes.domain.entities.Note;
import com.simplenotes.infrastructure.persistence.dao.CategoryDao;
import com.simplenotes.infrastructure.persistence.dao.NoteDao;
import com.simplenotes.infrastructure.persistence.dao.NoteDao.NoteDaoException;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class CategoryPanel extends JPanel {
    private JList<Category> categoryList;
    private DefaultListModel<Category> listModel;
    private JButton addButton;
    private JButton editButton;
    private JButton removeButton;
    private CategoryDao categoryDao;
    private NoteDao noteDao;
    private EditorPanel editorPanel;
    private static final Category DEFAULT_CATEGORY = new Category(-1L, "Uncategorized Notes", "Notes without category");

    public CategoryPanel(EditorPanel editorPanel) {
        try {
            this.editorPanel = editorPanel;
            this.categoryDao = new CategoryDao();
            this.noteDao = new NoteDao();
            initComponents();
            layoutComponents();
            setupListeners();
            refreshCategoryList();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error initializing database connection: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        listModel = new DefaultListModel<>();
        categoryList = new JList<>(listModel);
        categoryList.setCellRenderer(new CategoryListRenderer());
        
        // Tambahan konfigurasi untuk JList
        categoryList.setFixedCellHeight(-1); // Allow variable height cells
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setBackground(Color.WHITE);
        
        // Tambahkan kategori default ke model
        listModel.addElement(DEFAULT_CATEGORY);
        
        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        removeButton = new JButton("Remove");
    
        // Nonaktifkan tombol edit dan remove untuk kategori default
        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Category selected = categoryList.getSelectedValue();
                boolean isDefaultCategory = selected != null && selected.getId().equals(DEFAULT_CATEGORY.getId());
                editButton.setEnabled(!isDefaultCategory);
                removeButton.setEnabled(!isDefaultCategory);
            }
        });
    }
    

    private void layoutComponents() {
        setLayout(new BorderLayout(5, 5));
        add(new JScrollPane(categoryList), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    @SuppressWarnings("unused")
    private void setupListeners() {
        addButton.addActionListener(e -> handleAddCategory());
        editButton.addActionListener(e -> handleEditCategory());
        removeButton.addActionListener(e -> handleRemoveCategory());
        
        // Tambahkan mouse listener untuk categoryList
        categoryList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 1) {
                    Category selectedCategory = categoryList.getSelectedValue();
                    if (selectedCategory != null) {
                        showNotesDialog(selectedCategory);
                    }
                }
            }
        });
    }

    private void handleAddCategory() {
        String name = JOptionPane.showInputDialog(this,
            "Enter category name:", "Add Category",
            JOptionPane.PLAIN_MESSAGE);
            
        if (name != null && !name.trim().isEmpty()) {
            String description = JOptionPane.showInputDialog(this,
                "Enter category description:", "Add Category",
                JOptionPane.PLAIN_MESSAGE);
                
            try {
                Category category = new Category(null, name.trim(), description);
                categoryDao.save(category);
                refreshCategoryList();
            } catch (CategoryDao.CategoryDaoException e) {
                JOptionPane.showMessageDialog(this,
                    "Error creating category: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditCategory() {
        Category selected = categoryList.getSelectedValue();
        if (selected != null) {
            String name = JOptionPane.showInputDialog(this,
                "Enter new category name:",
                selected.getName());
                
            if (name != null && !name.trim().isEmpty()) {
                String description = JOptionPane.showInputDialog(this,
                    "Enter new category description:",
                    selected.getDescription());
                    
                try {
                    Category updatedCategory = new Category(
                        selected.getId(), name.trim(), description);
                    categoryDao.update(updatedCategory);
                    refreshCategoryList();
                } catch (CategoryDao.CategoryDaoException e) {
                    JOptionPane.showMessageDialog(this,
                        "Error updating category: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void handleRemoveCategory() {
        Category selected = categoryList.getSelectedValue();
        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this category?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    categoryDao.delete(selected.getId());
                    refreshCategoryList();
                } catch (CategoryDao.CategoryDaoException e) {
                    JOptionPane.showMessageDialog(this,
                        "Error deleting category: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void refreshCategoryList() {
        listModel.clear();
        // Selalu tambahkan kategori default terlebih dahulu
        listModel.addElement(DEFAULT_CATEGORY);
        try {
            List<Category> categories = categoryDao.findAll();
            categories.forEach(listModel::addElement);
        } catch (CategoryDao.CategoryDaoException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading categories: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private class CategoryListRenderer extends DefaultListCellRenderer {
        private final JSeparator separator;
    
        public CategoryListRenderer() {
            separator = new JSeparator(JSeparator.HORIZONTAL);
            separator.setForeground(Color.GRAY);
        }
    
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            JPanel panel = new JPanel(new BorderLayout(5, 0));
            panel.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            
            if (value instanceof Category) {
                Category category = (Category) value;
                boolean isDefaultCategory = category.getId().equals(DEFAULT_CATEGORY.getId());
    
                // Create labels for the category name and description
                JLabel nameLabel = new JLabel(category.getName());
                nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
                
                if (isDefaultCategory) {
                    // Untuk default category
                    nameLabel.setForeground(Color.BLACK); // Selalu hitam
                    
                    // Buat panel khusus untuk icon dan text dengan FlowLayout
                    JPanel iconTextPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); // padding 15 pixels
                    iconTextPanel.setOpaque(false);
                    
                    // Tambahkan icon ke panel terpisah
                    JLabel iconLabel = new JLabel(UIManager.getIcon("FileView.fileIcon"));
                    iconTextPanel.add(iconLabel);
                    iconTextPanel.add(nameLabel);
                    
                    JLabel descriptionLabel = new JLabel(category.getDescription());
                    descriptionLabel.setForeground(Color.GRAY); // Selalu abu-abu
                    
                    // Buat panel untuk description dengan padding kiri yang sama dengan text
                    JPanel descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); // 39 = icon width + padding
                    descriptionPanel.setOpaque(false);
                    descriptionPanel.add(descriptionLabel);
                    
                    // Panel untuk nama dan deskripsi
                    JPanel textPanel = new JPanel(new BorderLayout());
                    textPanel.setOpaque(false);
                    textPanel.add(iconTextPanel, BorderLayout.NORTH);
                    textPanel.add(descriptionPanel, BorderLayout.CENTER);
                    
                    // Buat panel separator
                    JPanel separatorPanel = new JPanel(new BorderLayout());
                    separatorPanel.setOpaque(false);
                    separatorPanel.add(textPanel, BorderLayout.CENTER);
                    
                    // Tambah padding sebelum separator
                    separatorPanel.add(Box.createVerticalStrut(5), BorderLayout.NORTH);
                    
                    // Tambah separator
                    JPanel sepWrapper = new JPanel(new BorderLayout());
                    sepWrapper.setOpaque(false);
                    sepWrapper.add(new JSeparator(), BorderLayout.CENTER);
                    sepWrapper.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                    
                    separatorPanel.add(sepWrapper, BorderLayout.SOUTH);
                    
                    // Tambahkan padding horizontal untuk keseluruhan panel
                    separatorPanel.setBorder(BorderFactory.createEmptyBorder(1, 0, 5, 0));
                    
                    return separatorPanel;
                
                } else {
                    // Untuk category biasa
                    nameLabel.setText((index) + ". " + category.getName());
                    nameLabel.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    
                    JLabel descriptionLabel = new JLabel(category.getDescription());
                    descriptionLabel.setForeground(isSelected ? list.getSelectionForeground() : Color.GRAY);
    
                    // Panel untuk nama dan deskripsi
                    JPanel textPanel = new JPanel(new BorderLayout());
                    textPanel.setOpaque(false);
                    textPanel.add(nameLabel, BorderLayout.NORTH);
                    textPanel.add(descriptionLabel, BorderLayout.CENTER);
                    
                    panel.add(textPanel, BorderLayout.CENTER);
                }
            }    
    
            return panel;
        }
    }
    
    

    private void showNotesDialog(Category category) {  try {
        // Get notes based on category
        List<Note> notes;
        if (category.getId().equals(DEFAULT_CATEGORY.getId())) {
            // Khusus untuk kategori default, ambil note tanpa category_id
            notes = noteDao.findByNullCategory();
        } else {
            notes = noteDao.findByCategoryId(category.getId());
        }
            
        if (notes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No notes found in this category",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    
        // Create a dialog for selecting a note
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Select Note from " + category.getName(), true);
        dialog.setLayout(new BorderLayout());
    
        // Create list model for notes
        DefaultListModel<Note> noteListModel = new DefaultListModel<>();
        notes.forEach(noteListModel::addElement);
    
        // Create JList to display notes with numbering
        JList<Note> noteList = new JList<>(noteListModel);
        // Modifikasi di dalam showNotesDialog pada bagian cell renderer
        noteList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                
                // Panel utama dengan padding
                JPanel mainPanel = new JPanel();
                mainPanel.setLayout(new BorderLayout());
                mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
                mainPanel.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        
                if (value instanceof Note) {
                    Note note = (Note) value;
        
                    // Content panel untuk menampung semua konten
                    JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
                    contentPanel.setOpaque(false);
        
                    // Panel untuk nomor dan judul (bagian atas)
                    JPanel titlePanel = new JPanel(new BorderLayout(5, 0));
                    titlePanel.setOpaque(false);
        
                    // Label untuk nomor
                    JLabel indexLabel = new JLabel((index + 1) + ". ");
                    indexLabel.setFont(indexLabel.getFont().deriveFont(Font.BOLD));
                    indexLabel.setForeground(isSelected ? list.getSelectionForeground() : Color.GRAY);
        
                    // Label untuk judul
                    String titleText = (note.getTitle() == null || note.getTitle().trim().isEmpty()) 
                        ? "Untitled Note" 
                        : note.getTitle();
                    JLabel titleLabel = new JLabel(titleText);
                    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN));
                    
                    // Atur warna judul
                    if (titleText.equals("Untitled Note")) {
                        titleLabel.setForeground(isSelected ? list.getSelectionForeground() : Color.GRAY);
                    } else {
                        titleLabel.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                    }
        
                    titlePanel.add(indexLabel, BorderLayout.WEST);
                    titlePanel.add(titleLabel, BorderLayout.CENTER);
        
                    // Panel untuk timestamp (bagian bawah)
                    JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
                    timePanel.setOpaque(false);
        
                    // Format timestamps
                    String createdAt = note.getCreatedAt() != null ? 
                        "Created: " + note.getCreatedAt().replace("T", " ") : "";
                    String updatedAt = note.getUpdatedAt() != null ? 
                        " | Updated: " + note.getUpdatedAt().replace("T", " ") : "";
        
                    // Label untuk timestamps
                    JLabel timestampLabel = new JLabel(createdAt + updatedAt);
                    timestampLabel.setFont(timestampLabel.getFont().deriveFont(Font.PLAIN, 9f));
                    timestampLabel.setForeground(isSelected ? list.getSelectionForeground() : Color.GRAY);
                    timePanel.add(timestampLabel);
        
                    // Tambahkan komponen ke content panel
                    contentPanel.add(titlePanel, BorderLayout.NORTH);
                    contentPanel.add(timePanel, BorderLayout.CENTER);
        
                    // Tambahkan content panel ke main panel
                    mainPanel.add(contentPanel, BorderLayout.CENTER);
                }
        
                return mainPanel;
            }
        });               
    
        // Add double click listener for note selection
        noteList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    Note selectedNote = noteList.getSelectedValue();
                    if (selectedNote != null) {
                        editorPanel.loadNote(selectedNote);
                        dialog.dispose();
                    }
                }
            }
        });
    
        // Create button panel
        JPanel buttonPanel = new JPanel();
        JButton selectButton = new JButton("Select");
        JButton cancelButton = new JButton("Cancel");
    
        selectButton.addActionListener(_ -> {
            Note selectedNote = noteList.getSelectedValue();
            if (selectedNote != null) {
                editorPanel.loadNote(selectedNote);
                dialog.dispose();
            }
        });
    
        cancelButton.addActionListener(_ -> dialog.dispose());
    
        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);
    
        // Add components to dialog
        dialog.add(new JScrollPane(noteList), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
    
        // Set dialog size and location
        dialog.setSize(300, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    } catch (NoteDaoException e) {
        JOptionPane.showMessageDialog(this,
            "Error loading notes: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
    }    
}
