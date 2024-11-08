// DeleteCategoryUseCase.java
package com.simplenotes.domain.usecases.category;

import com.simplenotes.domain.repositories.CategoryRepository;

public class DeleteCategoryUseCase {
    private final CategoryRepository categoryRepository;

    public DeleteCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void execute(Long id) {
        categoryRepository.delete(id);
    }
}
