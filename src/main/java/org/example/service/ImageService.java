package org.example.service;

import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.example.model.entity.Image;
import org.example.repository.ImageRepository;
import org.example.properties.MinioProperties;
import org.example.utils.DatabaseContextHolder;
import org.example.utils.DatabaseType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final ImageRepository imageRepository;

    @SneakyThrows
    public List<Long> uploadMultiple(List<MultipartFile> files) {
        createBucket();
        List<Long> imageIds = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty() || file.getOriginalFilename() == null) {
                throw new InvalidParameterException("One of the files is empty");
            }

            String fileName = generateFilename(file);
            try (InputStream inputStream = file.getInputStream()) {
                saveImage(inputStream, fileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file input stream", e);
            }
            DatabaseContextHolder.setDatabaseType(DatabaseType.MASTER);
            try {
                Image image = new Image();
                image.setName(fileName);
                image.setType(file.getContentType());
                imageRepository.save(image);
                imageIds.add(image.getId());
            }finally {
                DatabaseContextHolder.clear();
            }
        }

        return imageIds;
    }

    @SneakyThrows
    public Long uploadSingle(MultipartFile file) {
        if (file.isEmpty() || file.getOriginalFilename() == null) {
            throw new InvalidParameterException("File is empty or has no name");
        }

        createBucket();
        String fileName = generateFilename(file);
        try (InputStream inputStream = file.getInputStream()) {
            saveImage(inputStream, fileName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file input stream", e);
        }
        DatabaseContextHolder.setDatabaseType(DatabaseType.MASTER);
        try {
            Image image = new Image();
            image.setName(fileName);
            image.setType(file.getContentType());
            imageRepository.save(image);
            return image.getId();
        }finally {
            DatabaseContextHolder.clear();
        }

    }

    public byte[] getImage(String fileName) throws IOException {
        try {
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(fileName)
                            .build());

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                return outputStream.toByteArray();
            }
        } catch (ErrorResponseException e) {
            throw new IOException("Image not found: " + fileName, e);
        } catch (Exception e) {
            throw new IOException("Failed to get image from MinIO: " + fileName, e);
        }
    }

    public void deleteImage(String fileName){
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(fileName)
                            .build());
            imageRepository.delete(imageRepository.findByName(fileName).orElseThrow(()->new InvalidParameterException("Image not found")));
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image from MinIO", e);
        }
    }

    @SneakyThrows
    private void createBucket() {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());
        }
    }

    private String generateFilename(MultipartFile file) {
        String extension = getExtension(file);
        return UUID.randomUUID() + "." + extension;
    }

    private String getExtension(MultipartFile file) {
        return Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".") + 1);
    }

    private void saveImage(InputStream inputStream, String fileName) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(fileName)
                        .stream(inputStream, inputStream.available(), -1)
                        .build());
    }

    public Image getImageById(Long postImageId) {
        DatabaseContextHolder.setDatabaseType(DatabaseType.SLAVE);
        try {
            return imageRepository.findById(postImageId).orElseThrow(()->new InvalidParameterException("Image not found"));
        }finally {
            DatabaseContextHolder.clear();
        }
    }
}
