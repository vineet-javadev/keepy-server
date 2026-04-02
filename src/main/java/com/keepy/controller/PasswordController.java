package com.keepy.controller;

import com.keepy.entity.PasswordEntry;
import com.keepy.entity.User;
import com.keepy.payload.response.MessageResponse;
import com.keepy.repository.UserRepository;
import com.keepy.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passwords")
public class PasswordController {

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private UserRepository userRepository;

    // Helper method to get the currently logged-in user from the JWT
    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
    }

    // 1. Add a new Password to the Vault
    @PostMapping("/add")
    public ResponseEntity<?> addPassword(@RequestBody PasswordEntry entry, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            PasswordEntry savedEntry = passwordService.savePassword(entry, user);
            return ResponseEntity.ok(savedEntry);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error saving password: " + e.getMessage()));
        }
    }

    // 2. Get all Passwords (automatically decrypted)
    @GetMapping("/all")
    public List<PasswordEntry> getAllPasswords(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return passwordService.getAllPasswords(user);
    }

    // 3. Delete a Password Entry
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePassword(@PathVariable Long id, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            passwordService.deletePassword(id, user);
            return ResponseEntity.ok(new MessageResponse("Password deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage()));
        }
    }

    // 4. Search Passwords
    @GetMapping("/search")
    public List<PasswordEntry> search(@RequestParam String query, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return passwordService.searchPasswords(query, user);
    }
}