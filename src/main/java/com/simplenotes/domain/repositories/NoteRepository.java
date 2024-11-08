// NoteRepository.java
package com.simplenotes.domain.repositories;

import java.util.List;

import com.simplenotes.domain.entities.Note;

public interface NoteRepository {
    Note save(Note note);
    Note findById(Long id);
    List<Note> findAll();
    List<Note> findByCategoryId(Long categoryId);
    void delete(Long id);
}
