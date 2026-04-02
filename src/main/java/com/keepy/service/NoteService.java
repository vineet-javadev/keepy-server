package com.keepy.service;

import com.keepy.entity.Note;
import com.keepy.entity.User;
import com.keepy.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    // 1. Create a new blank or titled note
    public Note createNote(Note note, User user) {
        note.setUser(user);
        return noteRepository.save(note);
    }

    // 2. Update an existing note (Essential for Auto-save)
    public Note updateNote(Long id, Note noteDetails, User user) {
        Note note = noteRepository.findById(id)
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Note not found or unauthorized"));

        note.setTitle(noteDetails.getTitle());
        note.setContent(noteDetails.getContent());
        
        // Saving automatically triggers the @UpdateTimestamp in the entity
        return noteRepository.save(note);
    }

    // 3. Get all notes for a user (ordered by most recent)
    public List<Note> getAllNotes(User user) {
        return noteRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    // 4. Get a specific note by ID
    public Optional<Note> getNoteById(Long id, User user) {
        return noteRepository.findById(id)
                .filter(n -> n.getUser().getId().equals(user.getId()));
    }

    // 5. Delete a note
    public void deleteNote(Long id, User user) {
        Note note = noteRepository.findById(id)
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Note not found or unauthorized"));
        
        noteRepository.delete(note);
    }

    // 6. Search within notes
    public List<Note> searchNotes(String query, User user) {
        return noteRepository.findByTitleContainingIgnoreCaseAndUser(query, user);
    }
}