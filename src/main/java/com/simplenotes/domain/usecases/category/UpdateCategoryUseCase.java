// UpdateCategoryUseCase.java
package com.simplenotes.domain.usecases.category;

import com.simplenotes.domain.entities.Category;
import com.simplenotes.domain.repositories.CategoryRepository;

public class UpdateCategoryUseCase {
    private final CategoryRepository categoryRepository;

    public UpdateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category execute(Long id, String name, String description) {
        Category category = categoryRepository.findById(id);
        if (category != null) {
            category.setName(name);
            category.setDescription(description);
            return categoryRepository.save(category);
        }
        return null;
    }
}
