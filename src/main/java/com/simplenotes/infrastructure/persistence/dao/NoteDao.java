// NoteDao.java
package com.simplenotes.infrastructure.persistence.dao;

import com.simplenotes.domain.entities.Note;
import com.simplenotes.infrastructure.persistence.sqlite.SqliteConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NoteDao {
    private final Connection connection;

    public NoteDao() throws SQLException {
        this.connection = SqliteConnection.getConnection();
    }

    // Tambahkan method getConnection()
    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            return SqliteConnection.getConnection();
        }
        return connection;
    }

    public List<Note> findByNullCategory() throws NoteDaoException {
        String sql = "SELECT * FROM notes WHERE category_id IS NULL";
        List<Note> notes = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
            return notes;
        } catch (SQLException e) {
            throw new NoteDaoException("Error finding notes without category", e);
        }
    }

    public Note save(Note note) throws NoteDaoException {
        String sql = "INSERT INTO notes (title, content, category_id) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, note.getTitle());
            stmt.setString(2, note.getContent());
            if (note.getCategoryId() != null) {
                stmt.setLong(3, note.getCategoryId());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    note.setId(generatedKeys.getLong(1));
                    // Ambil timestamp yang baru dibuat
                    String selectSql = "SELECT created_at, updated_at FROM notes WHERE id = ?";
                    try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                        selectStmt.setLong(1, note.getId());
                        ResultSet rs = selectStmt.executeQuery();
                        if (rs.next()) {
                            note.setCreatedAt(rs.getString("created_at"));
                            note.setUpdatedAt(rs.getString("updated_at"));
                        }
                    }
                }
            }
            return note;
        } catch (SQLException e) {
            throw new NoteDaoException("Error saving note: " + e.getMessage());
        }
    }

    public Optional<Note> findById(Long id) {
        String sql = "SELECT * FROM notes WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToNote(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new NoteDaoException("Error finding note by ID: " + id, e);
        }
    }

    public List<Note> findAll() {
        String sql = "SELECT * FROM notes";
        List<Note> notes = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
            return notes;
        } catch (SQLException e) {
            throw new NoteDaoException("Error finding all notes", e);
        }
    }

    public List<Note> findByCategoryId(Long categoryId) {
        String sql = "SELECT * FROM notes WHERE category_id = ?";
        List<Note> notes = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(mapResultSetToNote(rs));
                }
                return notes;
            }
        } catch (SQLException e) {
            throw new NoteDaoException("Error finding notes by category ID: " + categoryId, e);
        }
    }

    public Note update(Note note) throws NoteDaoException {
        String sql = "UPDATE notes SET title = ?, content = ?, category_id = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, note.getTitle());
            stmt.setString(2, note.getContent());
            if (note.getCategoryId() != null) {
                stmt.setLong(3, note.getCategoryId());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            stmt.setLong(4, note.getId());
            
            stmt.executeUpdate();
            
            // Ambil updated_at terbaru
            String selectSql = "SELECT updated_at FROM notes WHERE id = ?";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setLong(1, note.getId());
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    note.setUpdatedAt(rs.getString("updated_at"));
                }
            }
            return note;
        } catch (SQLException e) {
            throw new NoteDaoException("Error updating note: " + e.getMessage());
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM notes WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new NoteDaoException("Deleting note failed, no rows affected.");
            }
        } catch (SQLException e) {
            throw new NoteDaoException("Error deleting note: " + id, e);
        }
    }

    private Note mapResultSetToNote(ResultSet rs) throws SQLException {
        return new Note(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("content"),
            rs.getObject("category_id") != null ? rs.getLong("category_id") : null,
            rs.getString("created_at"),
            rs.getString("updated_at")
        );
    }

    public static class NoteDaoException extends RuntimeException {
        public NoteDaoException(String message) {
            super(message);
        }

        public NoteDaoException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
