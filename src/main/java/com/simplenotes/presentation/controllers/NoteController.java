// NoteController.java
package com.simplenotes.presentation.controllers;

import com.simplenotes.domain.entities.Note;
import com.simplenotes.domain.usecases.note.CreateNoteUseCase;
import com.simplenotes.domain.usecases.note.DeleteNoteUseCase;
import com.simplenotes.domain.usecases.note.GetNoteUseCase;
import com.simplenotes.domain.usecases.note.UpdateNoteUseCase;
import com.simplenotes.infrastructure.persistence.sqlite.SqliteNoteRepository;

import java.util.List;

public class NoteController {
    private final CreateNoteUseCase createNoteUseCase;
    private final UpdateNoteUseCase updateNoteUseCase;
    private final DeleteNoteUseCase deleteNoteUseCase;
    private final GetNoteUseCase getNoteUseCase;

    public NoteController() {
        SqliteNoteRepository noteRepository = new SqliteNoteRepository();
        this.createNoteUseCase = new CreateNoteUseCase(noteRepository);
        this.updateNoteUseCase = new UpdateNoteUseCase(noteRepository);
        this.deleteNoteUseCase = new DeleteNoteUseCase(noteRepository);
        this.getNoteUseCase = new GetNoteUseCase(noteRepository);
    }

    public Note createNote(String title, String content, Long categoryId) {
        return createNoteUseCase.execute(title, content, categoryId);
    }

    public Note updateNote(Long id, String title, String content, Long categoryId) {
        return updateNoteUseCase.execute(id, title, content, categoryId);
    }

    public void deleteNote(Long id) {
        deleteNoteUseCase.execute(id);
    }

    public Note getNoteById(Long id) {
        return getNoteUseCase.execute(id);
    }

    public List<Note> getAllNotes() {
        return getNoteRepository().findAll();
    }

    public List<Note> getNotesByCategory(Long categoryId) {
        return getNoteRepository().findByCategoryId(categoryId);
    }

    private SqliteNoteRepository getNoteRepository() {
        return (SqliteNoteRepository) getNoteUseCase.noteRepository;
    }
}
