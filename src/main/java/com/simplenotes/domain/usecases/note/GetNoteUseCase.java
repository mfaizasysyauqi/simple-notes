// GetNoteUseCase.java
package com.simplenotes.domain.usecases.note;

import com.simplenotes.domain.entities.Note;
import com.simplenotes.domain.repositories.NoteRepository;

public class GetNoteUseCase {
    public final NoteRepository noteRepository;

    public GetNoteUseCase(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public Note execute(Long id) {
        return noteRepository.findById(id);
    }
}
