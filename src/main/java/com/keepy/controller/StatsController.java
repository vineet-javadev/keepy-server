package com.keepy.controller;

import com.keepy.repository.*;
import com.keepy.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired private PasswordRepository passwordRepository;
    @Autowired private FileRepository fileRepository;
    @Autowired private NoteRepository noteRepository;

    @GetMapping("/user-summary")
    public ResponseEntity<?> getUserSummary() {
        // Get current user ID from the JWT context
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        Map<String, Long> stats = new HashMap<>();
        stats.put("passwords", passwordRepository.countByUserId(userId));
        stats.put("files", fileRepository.countByUserId(userId));
        stats.put("notes", noteRepository.countByUserId(userId));

        return ResponseEntity.ok(stats);
    }
}