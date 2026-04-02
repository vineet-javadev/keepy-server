package com.keepy.service;

import com.keepy.entity.FileMetadata;
import com.keepy.entity.User;
import com.keepy.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private FileRepository fileRepository;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    // 1. Upload a file to S3 and save metadata to MySQL
    public FileMetadata uploadFile(MultipartFile file, User user) throws IOException {
        // Create a unique S3 key to avoid filename collisions (e.g., user1/uuid_filename.jpg)
        String s3Key = "user-" + user.getId() + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        // Prepare the S3 Upload Request
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .build();

        // Upload the file to AWS
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // Save metadata to database
        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(file.getOriginalFilename());
        metadata.setFileType(file.getContentType());
        metadata.setFileSize(file.getSize());
        metadata.setS3Key(s3Key);
        metadata.setUser(user);

        return fileRepository.save(metadata);
    }

    // 2. List all files for a specific user
    public List<FileMetadata> getUserFiles(User user) {
        return fileRepository.findByUser(user);
    }

    // 3. Delete file from both S3 and MySQL
    public void deleteFile(Long fileId, User user) {
        FileMetadata metadata = fileRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new RuntimeException("File not found or unauthorized"));

        // Delete from S3
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(metadata.getS3Key())
                .build();
        s3Client.deleteObject(deleteObjectRequest);

        // Delete metadata from MySQL
        fileRepository.delete(metadata);
    }
}