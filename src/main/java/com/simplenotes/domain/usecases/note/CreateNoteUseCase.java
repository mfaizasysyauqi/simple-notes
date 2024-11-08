// CreateNoteUseCase.java
package com.simplenotes.domain.usecases.note;

import com.simplenotes.domain.entities.Note;
import com.simplenotes.domain.repositories.NoteRepository;

public class CreateNoteUseCase {
    private final NoteRepository noteRepository;

    public CreateNoteUseCase(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public Note execute(String title, String content, Long categoryId) {
        Note note = new Note(null, title, content, categoryId);
        return noteRepository.save(note);
    }
}
