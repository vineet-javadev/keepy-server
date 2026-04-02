package com.keepy.repository;

import com.keepy.entity.Note;
import com.keepy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    Void deleteByUserId(Long userId);

    // Fetch all notes belonging to a specific user
    List<Note> findByUser(User user);

    // Search notes by title (case-insensitive) for a specific user
    List<Note> findByTitleContainingIgnoreCaseAndUser(String title, User user);

    // Retrieve notes ordered by the most recently updated
    List<Note> findByUserOrderByUpdatedAtDesc(User user);

    long countByUserId(Long userId);
}