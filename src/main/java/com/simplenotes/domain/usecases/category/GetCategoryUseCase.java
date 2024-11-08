// GetCategoryUseCase.java
package com.simplenotes.domain.usecases.category;

import com.simplenotes.domain.entities.Category;
import com.simplenotes.domain.repositories.CategoryRepository;

public class GetCategoryUseCase {
    public final CategoryRepository categoryRepository;

    public GetCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category execute(Long id) {
        return categoryRepository.findById(id);
    }
}
