// UpdateNoteUseCase.java
package com.simplenotes.domain.usecases.note;

import com.simplenotes.domain.entities.Note;
import com.simplenotes.domain.repositories.NoteRepository;

public class UpdateNoteUseCase {
    private final NoteRepository noteRepository;

    public UpdateNoteUseCase(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public Note execute(Long id, String title, String content, Long categoryId) {
        Note note = noteRepository.findById(id);
        if (note != null) {
            note.setTitle(title);
            note.setContent(content);
            note.setCategoryId(categoryId);
            return noteRepository.save(note);
        }
        return null;
    }
}
