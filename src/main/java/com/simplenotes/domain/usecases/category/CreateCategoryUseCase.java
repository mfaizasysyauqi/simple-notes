// CreateCategoryUseCase.java
package com.simplenotes.domain.usecases.category;

import com.simplenotes.domain.entities.Category;
import com.simplenotes.domain.repositories.CategoryRepository;

public class CreateCategoryUseCase {
    private final CategoryRepository categoryRepository;

    public CreateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category execute(String name, String description) {
        Category category = new Category(null, name, description);
        return categoryRepository.save(category);
    }
}
