package com.keepy.service;

import com.keepy.entity.FileMetadata;
import com.keepy.entity.User;
import com.keepy.repository.FileRepository;
import com.keepy.repository.NoteRepository;
import com.keepy.repository.PasswordRepository;
import com.keepy.repository.UserRepository;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * Retrieves all users for the admin management table.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Performs a "Nuclear Delete":
     * 1. Deletes physical files from AWS S3.
     * 2. Clears all passwords, notes, and file metadata from the DB.
     * 3. Deletes the user account.
     */
    @Transactional
    public void deleteUserCompletely(Long userId) {
        // 1. Fetch and Delete physical S3 objects
        List<FileMetadata> userFiles = fileRepository.findByUserId(userId);
        for (FileMetadata file : userFiles) {
            try {
                // Delete from S3
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(file.getS3Key())
                        .build();
                s3Client.deleteObject(deleteObjectRequest);
                // Delete metadata from MySQL
                fileRepository.delete(file);
            } catch (Exception e) {
                // Log the error but don't stop the process.
                // We don't want a network error to prevent account deletion.
                System.err.println("CRITICAL: Failed to delete S3 object for key: " + file.getS3Key());
            }
        }
        // 2. Clear related records in the database
        // These methods must be defined in your Repositories
        // passwordRepository.deleteByUserId(userId);
        // System.out.println("AdminService: Deleted passwords for userId: " + userId);
        // noteRepository.deleteByUserId(userId);
        // System.out.println("AdminService: Deleted notes for userId: " + userId);
        // fileRepository.deleteByUserId(userId);
        // System.out.println("AdminService: Deleted file metadata for userId: " + userId);

        // 3. Delete the actual User entity

        System.out.println("before");
        userRepository.deleteById(userId);
        System.out.println("after");
    }

    /**
     * Returns system-wide statistics for the Admin Dashboard.
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long totalFiles = fileRepository.count();

        // Summing the fileSize column for all records to get total S3 usage
        long totalStorageBytes = fileRepository.findAll().stream()
                .mapToLong(FileMetadata::getFileSize)
                .sum();

        stats.put("totalUsers", totalUsers);
        stats.put("totalFiles", totalFiles);
        stats.put("totalStorageUsed", totalStorageBytes);
        return stats;
    }
}