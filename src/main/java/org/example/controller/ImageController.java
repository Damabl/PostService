package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.model.entity.Image;
import org.example.repository.ImageRepository;
import org.example.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/images")
public class ImageController {
    private final ImageService imageService;
    private final ImageRepository imageRepository;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) throws IOException {
        String filename = imageRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Image not found")).getName();
        byte[] imageBytes = imageService.getImage(filename);
        return ResponseEntity.ok().body(imageBytes);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMultipleImages(@RequestParam("files") List<MultipartFile> files) {
        try {
            List<Long> imageIds = imageService.uploadMultiple(files);
            return ResponseEntity.ok(imageIds);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to upload images");
        }
    }

    @PostMapping("/upload/single")
    public ResponseEntity<?> uploadSingleImage(@RequestParam("file") MultipartFile file) {
        try {
            Long imageId = imageService.uploadSingle(file);
            return ResponseEntity.ok(imageId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to upload image");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteImage(@PathVariable Long id) {
        String filename = imageRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Image not found")).getName();
        try {
            imageService.deleteImage(filename);
            return ResponseEntity.ok("Image deleted");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete image");
        }
    }
}
