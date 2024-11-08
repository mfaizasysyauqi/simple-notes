// CategoryChangePublisher.java
package com.simplenotes.domain.events;

import com.simplenotes.domain.entities.Category;
import java.util.ArrayList;
import java.util.List;

public class CategoryChangePublisher {
    private static CategoryChangePublisher instance;
    private List<CategoryChangeListener> listeners = new ArrayList<>();

    private CategoryChangePublisher() {}

    public static CategoryChangePublisher getInstance() {
        if (instance == null) {
            instance = new CategoryChangePublisher();
        }
        return instance;
    }

    public void addListener(CategoryChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(CategoryChangeListener listener) {
        listeners.remove(listener);
    }

    public void notifyCategoryAdded(Category category) {
        for (CategoryChangeListener listener : listeners) {
            listener.onCategoryAdded(category);
        }
    }

    public void notifyCategoryUpdated(Category category) {
        for (CategoryChangeListener listener : listeners) {
            listener.onCategoryUpdated(category);
        }
    }

    public void notifyCategoryDeleted(Long categoryId) {
        for (CategoryChangeListener listener : listeners) {
            listener.onCategoryDeleted(categoryId);
        }
    }
}
