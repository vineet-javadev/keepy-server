package com.keepy.repository;

import com.keepy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by their username (useful for login)
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // Check if a username already exists (useful for signup validation)
    Boolean existsByUsername(String username);

    // Check if an email already exists (useful for signup validation)
    Boolean existsByEmail(String email);
}