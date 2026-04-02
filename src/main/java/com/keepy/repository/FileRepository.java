package com.keepy.repository;

import com.keepy.entity.FileMetadata;
import com.keepy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileMetadata, Long> {

    // Retrieve all file records belonging to a specific user
    List<FileMetadata> findByUser(User user);

    List<FileMetadata> findByUserId(Long userId);

    // Find a specific file by its unique S3 key
    Optional<FileMetadata> findByS3Key(String s3Key);

    // Find a file by ID and ensure it belongs to the correct user (Security check)
    Optional<FileMetadata> findByIdAndUser(Long id, User user);

    long countByUserId(Long userId);

    Void deleteByUserId(Long userId);

}