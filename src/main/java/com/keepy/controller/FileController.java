package com.keepy.controller;

import com.keepy.entity.FileMetadata;
import com.keepy.entity.User;
import com.keepy.payload.response.MessageResponse;
import com.keepy.repository.UserRepository;
import com.keepy.service.FileService;
import com.keepy.service.S3PresignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private S3PresignService presignService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
    }

    // 1. Upload a File
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            FileMetadata metadata = fileService.uploadFile(file, user);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Upload failed: " + e.getMessage()));
        }
    }

    // 2. Get All File Metadata for the User
    @GetMapping("/all")
    public List<FileMetadata> getAllFiles(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return fileService.getUserFiles(user);
    }

    // 3. Generate a Temporary Download Link
    @GetMapping("/download/{id}")
    public ResponseEntity<?> getDownloadUrl(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        try {
            // Find the file and ensure the user owns it
            List<FileMetadata> userFiles = fileService.getUserFiles(user);
            FileMetadata file = userFiles.stream()
                    .filter(f -> f.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unauthorized or File not found"));

            String url = presignService.generatePresignedUrl(file.getS3Key());
            return ResponseEntity.ok(Map.of("downloadUrl", url));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage()));
        }
    }

    // 4. Delete a File
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            fileService.deleteFile(id, user);
            return ResponseEntity.ok(new MessageResponse("File deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage()));
        }
    }
}