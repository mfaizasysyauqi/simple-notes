// SqliteNoteRepository.java
package com.simplenotes.infrastructure.persistence.sqlite;

import com.simplenotes.domain.entities.Note;
import com.simplenotes.domain.repositories.NoteRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteNoteRepository implements NoteRepository {
    @Override
    public Note save(Note note) {
        String sql;
        if (note.getId() == null) {
            sql = "INSERT INTO notes (title, content, category_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE notes SET title = ?, content = ?, category_id = ?, updated_at = ? WHERE id = ?";
        }

        try (Connection conn = SqliteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (note.getId() == null) {
                pstmt.setString(1, note.getTitle());
                pstmt.setString(2, note.getContent());
                // Handle null categoryId
                if (note.getCategoryId() != null) {
                    pstmt.setLong(3, note.getCategoryId());
                } else {
                    pstmt.setNull(3, Types.INTEGER);
                }
                pstmt.setString(4, note.getCreatedAt());
                pstmt.setString(5, note.getUpdatedAt());
            } else {
                pstmt.setString(1, note.getTitle());
                pstmt.setString(2, note.getContent());
                // Handle null categoryId
                if (note.getCategoryId() != null) {
                    pstmt.setLong(3, note.getCategoryId());
                } else {
                    pstmt.setNull(3, Types.INTEGER);
                }
                pstmt.setString(4, note.getUpdatedAt());
                pstmt.setLong(5, note.getId());
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating/Updating note failed, no rows affected.");
            }

            if (note.getId() == null) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        note.setId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating note failed, no ID obtained.");
                    }
                }
            }
            
            return note;
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public Note findById(Long id) {
        String sql = "SELECT * FROM notes WHERE id = ?";
        
        try (Connection conn = SqliteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToNote(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Note> findAll() {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes";
        
        try (Connection conn = SqliteConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
            return notes;
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Note> findByCategoryId(Long categoryId) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes WHERE category_id = ?";
        
        try (Connection conn = SqliteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
            return notes;
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM notes WHERE id = ?";
        
        try (Connection conn = SqliteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    private Note mapResultSetToNote(ResultSet rs) throws SQLException {
        Long categoryId = rs.getLong("category_id");
        if (rs.wasNull()) {
            categoryId = null;
        }
        
        return new Note(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("content"),
            categoryId,  // Use the potentially null categoryId
            rs.getString("created_at"),
            rs.getString("updated_at")
        );
    }
}
