package com.keepy.controller;

import com.keepy.entity.Note;
import com.keepy.entity.User;
import com.keepy.payload.response.MessageResponse;
import com.keepy.repository.UserRepository;
import com.keepy.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
    }

    // 1. Create a new note
    @PostMapping("/add")
    public ResponseEntity<Note> createNote(@Valid @RequestBody Note note, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(noteService.createNote(note, user));
    }

    // 2. Get all notes for the user
    @GetMapping("/all")
    public List<Note> getAllNotes(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return noteService.getAllNotes(user);
    }

    // 3. Get a specific note
    @GetMapping("/{id}")
    public ResponseEntity<?> getNote(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return noteService.getNoteById(id, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. Update/Auto-save a note
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateNote(@PathVariable Long id, @RequestBody Note note, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            Note updatedNote = noteService.updateNote(id, note, user);
            return ResponseEntity.ok(updatedNote);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // 5. Delete a note
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            noteService.deleteNote(id, user);
            return ResponseEntity.ok(new MessageResponse("Note deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage()));
        }
    }

    // 6. Search notes
    @GetMapping("/search")
    public List<Note> search(@RequestParam String query, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return noteService.searchNotes(query, user);
    }
}