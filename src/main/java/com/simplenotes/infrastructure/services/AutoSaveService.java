// AutoSaveService.java
package com.simplenotes.infrastructure.services;

import com.simplenotes.domain.entities.Note;
import com.simplenotes.infrastructure.persistence.dao.NoteDao;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

public class AutoSaveService {
    private final Timer timer;
    private final NoteDao noteDao;
    private final AtomicReference<Note> currentNote;
    private final AtomicReference<String> currentTitle;
    private final AtomicReference<String> currentContent;
    private final AtomicReference<Long> currentCategoryId;
    private boolean isDirty;
    
    public AutoSaveService(NoteDao noteDao) {
        this.noteDao = noteDao;
        this.timer = new Timer(true); // Run as daemon thread
        this.currentNote = new AtomicReference<>();
        this.currentTitle = new AtomicReference<>("");
        this.currentContent = new AtomicReference<>("");
        this.currentCategoryId = new AtomicReference<>();
        this.isDirty = false;
    }

    public void startAutoSave() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isDirty) {
                    saveCurrentState();
                    isDirty = false;
                }
            }
        }, 5000, 5000); // Run every 5 seconds
    }

    public void stopAutoSave() {
        timer.cancel();
        // Final save before stopping
        if (isDirty) {
            saveCurrentState();
        }
    }

    public void updateCurrentState(String title, String content, Long categoryId) {
        currentTitle.set(title);
        currentContent.set(content);
        currentCategoryId.set(categoryId);
        isDirty = true;
    }

    public void setCurrentNote(Note note) {
        currentNote.set(note);
        if (note != null) {
            currentTitle.set(note.getTitle());
            currentContent.set(note.getContent());
            currentCategoryId.set(note.getCategoryId());
        } else {
            currentTitle.set("");
            currentContent.set("");
            currentCategoryId.set(null);
        }
        isDirty = false;
    }

    private void saveCurrentState() {
        try {
            Note note = currentNote.get();
            String title = currentTitle.get();
            String content = currentContent.get();
            Long categoryId = currentCategoryId.get();

            // Only save if there's actual content
            if (!title.trim().isEmpty() || !content.trim().isEmpty()) {
                if (note == null) {
                    // Create new autosaved note
                    note = new Note(null, title, content, categoryId);
                    note = noteDao.save(note);
                    currentNote.set(note);
                } else {
                    // Update existing note
                    note.setTitle(title);
                    note.setContent(content);
                    note.setCategoryId(categoryId);
                    noteDao.update(note);
                }
            }
        } catch (Exception e) {
            // Log error or handle appropriately
            e.printStackTrace();
        }
    }

    public Note getLastAutoSavedNote() {
        return currentNote.get();
    }
}
