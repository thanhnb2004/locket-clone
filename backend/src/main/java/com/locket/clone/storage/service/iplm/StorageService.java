package com.locket.clone.storage.service.iplm;

import com.locket.clone.common.ApiException;
import com.locket.clone.storage.service.IStorageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

/**
 * Stores moment images in an S3 bucket (MinIO locally, AWS S3 in production).
 */
@Service
public class StorageService implements IStorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private final S3Client s3;
    private final String bucket;

    public StorageService(S3Client s3, @Value("${app.s3.bucket}") String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    /**
     * Ensures the bucket exists on startup. Retries a few times so the app tolerates MinIO/S3
     * still coming up (e.g. when started together by Docker Compose).
     */
    @PostConstruct
    void init() {
        int maxAttempts = 15;
        for (int attempt = 1; ; attempt++) {
            try {
                ensureBucket();
                log.info("S3 bucket '{}' is ready", bucket);
                return;
            } catch (SdkException e) {
                if (attempt >= maxAttempts) {
                    throw new IllegalStateException("Could not reach S3/MinIO or ensure bucket: " + bucket, e);
                }
                log.warn("S3/MinIO not ready (attempt {}/{}): {}", attempt, maxAttempts, e.getMessage());
                sleep(2000);
            }
        }
    }

    private void ensureBucket() {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            log.info("Created S3 bucket '{}'", bucket);
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Image file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        }

        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> "";
        };

        String key = UUID.randomUUID() + extension;
        try {
            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException | SdkException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store image");
        }
        return key;
    }

    @Override
    public byte[] load(String key) {
        try {
            return s3.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()).asByteArray();
        } catch (NoSuchKeyException e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Image not found");
        } catch (SdkException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load image");
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for S3/MinIO", ie);
        }
    }
}
