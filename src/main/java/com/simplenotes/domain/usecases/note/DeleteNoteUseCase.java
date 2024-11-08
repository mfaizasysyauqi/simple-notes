// DeleteNoteUseCase.java
package com.simplenotes.domain.usecases.note;

import com.simplenotes.domain.repositories.NoteRepository;

public class DeleteNoteUseCase {
    private final NoteRepository noteRepository;

    public DeleteNoteUseCase(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public void execute(Long id) {
        noteRepository.delete(id);
    }
}
