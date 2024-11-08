// SqliteCategoryRepository.java
package com.simplenotes.infrastructure.persistence.sqlite;

import com.simplenotes.domain.entities.Category;
import com.simplenotes.domain.repositories.CategoryRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteCategoryRepository implements CategoryRepository {
    
    @Override
    public Category save(Category category) {
        String sql;
        if (category.getId() == null) {
            sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        } else {
            sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
        }

        try (Connection conn = SqliteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            
            if (category.getId() != null) {
                pstmt.setLong(3, category.getId());
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating/Updating category failed, no rows affected.");
            }

            if (category.getId() == null) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        category.setId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating category failed, no ID obtained.");
                    }
                }
            }
            
            return category;
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public Category findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        
        try (Connection conn = SqliteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCategory(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        
        try (Connection conn = SqliteConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
            return categories;
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        
        try (Connection conn = SqliteConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        return new Category(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description")
        );
    }
}
