package com.klkt.supervision.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.type", havingValue = "minio")
public class MinioConfig {
    
    private final StorageProperties storageProperties;
    
    @Bean
    public MinioClient minioClient() {
        try {
            StorageProperties.MinioStorage minio = storageProperties.getMinio();
            
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minio.getEndpoint())
                    .credentials(minio.getAccessKey(), minio.getSecretKey())
                    .build();
            
            // Auto-create bucket if enabled
            if (minio.isAutoCreateBucket()) {
                boolean bucketExists = minioClient.bucketExists(
                        BucketExistsArgs.builder()
                                .bucket(minio.getBucket())
                                .build()
                );
                
                if (!bucketExists) {
                    minioClient.makeBucket(
                            MakeBucketArgs.builder()
                                    .bucket(minio.getBucket())
                                    .build()
                    );
                    log.info("Created MinIO bucket: {}", minio.getBucket());
                }
            }
            
            log.info("MinIO client initialized successfully");
            return minioClient;
            
        } catch (Exception e) {
            log.error("Failed to initialize MinIO client", e);
            throw new RuntimeException("Failed to initialize MinIO client", e);
        }
    }
}
