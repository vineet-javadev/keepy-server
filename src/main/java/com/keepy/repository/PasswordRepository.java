package com.keepy.repository;

import com.keepy.entity.PasswordEntry;
import com.keepy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PasswordRepository extends JpaRepository<PasswordEntry, Long> {

    // Retrieve all password entries belonging to a specific logged-in user
    List<PasswordEntry> findByUser(User user);

    long countByUserId(Long userId);

    // Search passwords by website name for a specific user (case-insensitive)
    List<PasswordEntry> findByWebsiteNameContainingIgnoreCaseAndUser(String websiteName, User user);

    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
}