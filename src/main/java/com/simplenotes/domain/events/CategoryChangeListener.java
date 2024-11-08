// CategoryChangeListener.java
package com.simplenotes.domain.events;

import com.simplenotes.domain.entities.Category;

public interface CategoryChangeListener {
    void onCategoryAdded(Category category);
    void onCategoryUpdated(Category category);
    void onCategoryDeleted(Long categoryId);
}
