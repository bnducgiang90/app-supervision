package com.klkt.supervision.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    
    private String type = "local"; // minio or local
    
    private LocalStorage local = new LocalStorage();
    private MinioStorage minio = new MinioStorage();
    
    @Data
    public static class LocalStorage {
        private String uploadDir = "./uploads";
    }
    
    @Data
    public static class MinioStorage {
        private String endpoint = "http://localhost:9000";
        private String accessKey = "minioadmin";
        private String secretKey = "minioadmin";
        private String bucket = "chat-files";
        private boolean autoCreateBucket = true;
    }
}
