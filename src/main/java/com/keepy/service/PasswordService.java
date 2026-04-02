package com.keepy.service;

import com.keepy.entity.PasswordEntry;
import com.keepy.entity.User;
import com.keepy.repository.PasswordRepository;
import com.keepy.util.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PasswordService {

    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private EncryptionUtils encryptionUtils;

    // 1. Create and Encrypt a new password entry
    public PasswordEntry savePassword(PasswordEntry entry, User user) throws Exception {
        String iv = encryptionUtils.generateIv();
        String encrypted = encryptionUtils.encrypt(entry.getEncryptedPassword(), iv);
        
        entry.setIv(iv);
        entry.setEncryptedPassword(encrypted);
        entry.setUser(user);
        
        return passwordRepository.save(entry);
    }

    // 2. Get all passwords for a user and Decrypt them
    public List<PasswordEntry> getAllPasswords(User user) {
        List<PasswordEntry> entries = passwordRepository.findByUser(user);
        
        entries.forEach(entry -> {
            try {
                String decrypted = encryptionUtils.decrypt(entry.getEncryptedPassword(), entry.getIv());
                entry.setEncryptedPassword(decrypted);
            } catch (Exception e) {
                entry.setEncryptedPassword("DECRYPTION_ERROR");
            }
        });
        
        return entries;
    }

    // 3. Delete an entry (ensure it belongs to the user)
    public void deletePassword(Long id, User user) {
        Optional<PasswordEntry> entry = passwordRepository.findById(id);
        if (entry.isPresent() && entry.get().getUser().getId().equals(user.getId())) {
            passwordRepository.deleteById(id);
        } else {
            throw new RuntimeException("Unauthorized or Entry not found");
        }
    }
    
    // 4. Search logic
    public List<PasswordEntry> searchPasswords(String query, User user) {
        return passwordRepository.findByWebsiteNameContainingIgnoreCaseAndUser(query, user);
    }
}