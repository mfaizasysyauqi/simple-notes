// CategoryController.java
package com.simplenotes.presentation.controllers;

import com.simplenotes.domain.entities.Category;
import com.simplenotes.domain.usecases.category.CreateCategoryUseCase;
import com.simplenotes.domain.usecases.category.DeleteCategoryUseCase;
import com.simplenotes.domain.usecases.category.GetCategoryUseCase;
import com.simplenotes.domain.usecases.category.UpdateCategoryUseCase;
import com.simplenotes.infrastructure.persistence.sqlite.SqliteCategoryRepository;

import java.util.List;

public class CategoryController {
    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final GetCategoryUseCase getCategoryUseCase;

    public CategoryController() {
        SqliteCategoryRepository categoryRepository = new SqliteCategoryRepository();
        this.createCategoryUseCase = new CreateCategoryUseCase(categoryRepository);
        this.updateCategoryUseCase = new UpdateCategoryUseCase(categoryRepository);
        this.deleteCategoryUseCase = new DeleteCategoryUseCase(categoryRepository);
        this.getCategoryUseCase = new GetCategoryUseCase(categoryRepository);
    }

    public Category createCategory(String name, String description) {
        return createCategoryUseCase.execute(name, description);
    }

    public Category updateCategory(Long id, String name, String description) {
        return updateCategoryUseCase.execute(id, name, description);
    }

    public void deleteCategory(Long id) {
        deleteCategoryUseCase.execute(id);
    }

    public Category getCategoryById(Long id) {
        return getCategoryUseCase.execute(id);
    }

    public List<Category> getAllCategories() {
        return getCategoryRepository().findAll();
    }

    private SqliteCategoryRepository getCategoryRepository() {
        return (SqliteCategoryRepository) getCategoryUseCase.categoryRepository;
    }
}
