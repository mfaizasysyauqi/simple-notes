// CategoryDao.java
package com.simplenotes.infrastructure.persistence.dao;

import com.simplenotes.domain.entities.Category;
import com.simplenotes.domain.events.CategoryChangePublisher;
import com.simplenotes.infrastructure.persistence.sqlite.SqliteConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDao {
    private final Connection connection;
    private CategoryChangePublisher publisher = CategoryChangePublisher.getInstance();
    
    public CategoryDao() throws SQLException {
        this.connection = SqliteConnection.getConnection();
    }

    public Category save(Category category) {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new CategoryDaoException("Creating category failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getLong(1));
                } else {
                    throw new CategoryDaoException("Creating category failed, no ID obtained.");
                }
            }
            Category savedCategory = category;
            publisher.notifyCategoryAdded(savedCategory);
            return savedCategory;
        } catch (SQLException e) {
            throw new CategoryDaoException("Error saving category", e);
        }
    }

    public Optional<Category> findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new CategoryDaoException("Error finding category by ID: " + id, e);
        }
    }

    public List<Category> findAll() {
        String sql = "SELECT * FROM categories";
        List<Category> categories = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
            return categories;
        } catch (SQLException e) {
            throw new CategoryDaoException("Error finding all categories", e);
        }
    }

    public Category update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setLong(3, category.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new CategoryDaoException("Updating category failed, no rows affected.");
            }
            
            Category updatedCategory = category;
            publisher.notifyCategoryUpdated(updatedCategory);
            return updatedCategory;
        } catch (SQLException e) {
            throw new CategoryDaoException("Error updating category: " + category.getId(), e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new CategoryDaoException("Deleting category failed, no rows affected.");
            }
            publisher.notifyCategoryDeleted(id);
        } catch (SQLException e) {
            throw new CategoryDaoException("Error deleting category: " + id, e);
        }
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        return new Category(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description")
        );
    }

    // Custom exception class
    public static class CategoryDaoException extends RuntimeException {
        public CategoryDaoException(String message) {
            super(message);
        }

        public CategoryDaoException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
