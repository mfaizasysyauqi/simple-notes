// CategoryRepository.java
package com.simplenotes.domain.repositories;

import java.util.List;
import com.simplenotes.domain.entities.Category;


public interface CategoryRepository {
    Category save(Category category);
    Category findById(Long id);
    List<Category> findAll();
    void delete(Long id);
}
