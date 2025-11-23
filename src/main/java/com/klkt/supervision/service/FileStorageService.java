package com.klkt.supervision.service;

import com.klkt.supervision.config.StorageProperties;
import com.klkt.supervision.dto.FileUploadResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public interface FileStorageService {
    Mono<FileUploadResponse> uploadFile(FilePart filePart, String folder);
    Mono<String> getFileUrl(String storagePath);
    Mono<Void> deleteFile(String storagePath);
}

// ============= MinIO Implementation =============

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "minio")
class MinioFileStorageService implements FileStorageService {
    
    private final MinioClient minioClient;
    private final StorageProperties storageProperties;
    
    public MinioFileStorageService(MinioClient minioClient, StorageProperties storageProperties) {
        this.minioClient = minioClient;
        this.storageProperties = storageProperties;
    }
    
    @Override
    public Mono<FileUploadResponse> uploadFile(FilePart filePart, String folder) {
        String originalFilename = filePart.filename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID() + extension;
        String objectName = folder + "/" + uniqueFilename;
        String bucket = storageProperties.getMinio().getBucket();
        
        // Convert DataBuffer to byte array reactively
        return DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                })
                .flatMap(bytes -> Mono.fromCallable(() -> {
                    InputStream inputStream = new java.io.ByteArrayInputStream(bytes);
                    long fileSize = bytes.length;
                    
                    // Upload to MinIO
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucket)
                                    .object(objectName)
                                    .stream(inputStream, fileSize, -1)
                                    .contentType(getContentType(extension))
                                    .build()
                    );
                    
                    inputStream.close();
                    
                        String fileUrl = storageProperties.getMinio().getEndpoint() + 
                                       "/" + bucket + "/" + objectName;
                        
                        // Generate thumbnail for images
                        String thumbnailUrl = null;
                        String fileType = getContentType(extension);
                        if (fileType.startsWith("image/")) {
                            // For MinIO, use original image as thumbnail
                            // In production, you might want to generate and upload a resized version
                            thumbnailUrl = fileUrl;
                        }
                        // For videos, thumbnail will be null - frontend will handle it
                        
                        log.info("File uploaded to MinIO: {}", objectName);
                        
                        return FileUploadResponse.builder()
                                .fileName(originalFilename)
                                .fileUrl(fileUrl)
                                .storagePath(objectName)
                                .fileSize(fileSize)
                                .fileType(fileType)
                                .thumbnailUrl(thumbnailUrl)
                                .build();
                }).onErrorMap(e -> new RuntimeException("Failed to upload file to MinIO", e)));
    }
    
    @Override
    public Mono<String> getFileUrl(String storagePath) {
        return Mono.fromCallable(() -> {
            String bucket = storageProperties.getMinio().getBucket();
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(io.minio.http.Method.GET)
                            .bucket(bucket)
                            .object(storagePath)
                            .expiry(60 * 60 * 24) // 24 hours
                            .build()
            );
        });
    }
    
    @Override
    public Mono<Void> deleteFile(String storagePath) {
        return Mono.fromRunnable(() -> {
            try {
                String bucket = storageProperties.getMinio().getBucket();
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucket)
                                .object(storagePath)
                                .build()
                );
                log.info("File deleted from MinIO: {}", storagePath);
            } catch (Exception e) {
                log.error("Failed to delete file from MinIO", e);
            }
        });
    }
    
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }
    
    private String getContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".mp4" -> "video/mp4";
            case ".avi" -> "video/x-msvideo";
            case ".mov" -> "video/quicktime";
            default -> "application/octet-stream";
        };
    }
    
    private byte[] generateThumbnailFromBytes(byte[] imageBytes) throws Exception {
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(imageBytes);
        java.awt.image.BufferedImage originalImage = javax.imageio.ImageIO.read(bais);
        if (originalImage == null) {
            throw new Exception("Could not read image");
        }

        int thumbnailWidth = 200;
        int thumbnailHeight = 200;

        double scale = Math.min(
                (double) thumbnailWidth / originalImage.getWidth(),
                (double) thumbnailHeight / originalImage.getHeight()
        );

        int scaledWidth = (int) (originalImage.getWidth() * scale);
        int scaledHeight = (int) (originalImage.getHeight() * scale);

        java.awt.image.BufferedImage thumbnail = new java.awt.image.BufferedImage(
                scaledWidth, scaledHeight, java.awt.image.BufferedImage.TYPE_INT_RGB
        );
        java.awt.Graphics2D g = thumbnail.createGraphics();
        g.setRenderingHint(
                java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(thumbnail, "jpg", baos);
        return baos.toByteArray();
    }
}

// ============= Local File System Implementation =============

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
class LocalFileStorageService implements FileStorageService {
    
    private final StorageProperties storageProperties;
    private final Path rootLocation;
    
    public LocalFileStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
        this.rootLocation = Paths.get(storageProperties.getLocal().getUploadDir());
        
        try {
            Files.createDirectories(rootLocation);
            log.info("Upload directory created: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }
    
    @Override
    public Mono<FileUploadResponse> uploadFile(FilePart filePart, String folder) {
        String originalFilename = filePart.filename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID() + extension;
        
        // Convert DataBuffer to byte array reactively
        return DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                })
                .flatMap(bytes -> Mono.fromCallable(() -> {
                    Path folderPath = rootLocation.resolve(folder);
                    Files.createDirectories(folderPath);
                    
                    Path destinationFile = folderPath.resolve(uniqueFilename);
                    
                    // Write bytes to file
                    Files.write(destinationFile, bytes);
                    
                    long fileSize = Files.size(destinationFile);
                    String storagePath = folder + "/" + uniqueFilename;
                    String fileUrl = "/api/files/" + storagePath;
                    
                    // Generate thumbnail for images and videos
                    String thumbnailUrl = null;
                    String fileType = getContentType(extension);
                    
                    if (fileType.startsWith("image/")) {
                        // Generate thumbnail for images
                        try {
                            byte[] thumbnailData = generateThumbnailFromBytes(bytes);
                            String thumbnailFilename = "thumb_" + uniqueFilename;
                            Path thumbnailFile = folderPath.resolve(thumbnailFilename);
                            Files.write(thumbnailFile, thumbnailData);
                            
                            String thumbnailStoragePath = folder + "/" + thumbnailFilename;
                            thumbnailUrl = "/api/files/" + thumbnailStoragePath;
                            log.info("Thumbnail generated: {}", thumbnailFile.toAbsolutePath());
                        } catch (Exception e) {
                            log.warn("Failed to generate thumbnail for image: {}", e.getMessage());
                            // Fallback to original image
                            thumbnailUrl = fileUrl;
                        }
                    } else if (fileType.startsWith("video/")) {
                        // For videos, thumbnail will be null - frontend will use video element's first frame
                        thumbnailUrl = null;
                    }
                    
                    log.info("File uploaded locally: {}", destinationFile.toAbsolutePath());
                    
                    return FileUploadResponse.builder()
                            .fileName(originalFilename)
                            .fileUrl(fileUrl)
                            .storagePath(storagePath)
                            .fileSize(fileSize)
                            .fileType(fileType)
                            .thumbnailUrl(thumbnailUrl)
                            .build();
                }).onErrorMap(IOException.class, e -> new RuntimeException("Failed to upload file locally", e)));
    }
    
    @Override
    public Mono<String> getFileUrl(String storagePath) {
        return Mono.just("/api/files/" + storagePath);
    }
    
    @Override
    public Mono<Void> deleteFile(String storagePath) {
        return Mono.fromRunnable(() -> {
            try {
                Path filePath = rootLocation.resolve(storagePath);
                Files.deleteIfExists(filePath);
                log.info("File deleted locally: {}", filePath.toAbsolutePath());
            } catch (IOException e) {
                log.error("Failed to delete file locally", e);
            }
        });
    }
    
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }
    
    private String getContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".mp4" -> "video/mp4";
            case ".avi" -> "video/x-msvideo";
            case ".mov" -> "video/quicktime";
            default -> "application/octet-stream";
        };
    }
    
    private byte[] generateThumbnailFromBytes(byte[] imageBytes) throws Exception {
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(imageBytes);
        java.awt.image.BufferedImage originalImage = javax.imageio.ImageIO.read(bais);
        if (originalImage == null) {
            throw new Exception("Could not read image");
        }

        int thumbnailWidth = 200;
        int thumbnailHeight = 200;

        double scale = Math.min(
                (double) thumbnailWidth / originalImage.getWidth(),
                (double) thumbnailHeight / originalImage.getHeight()
        );

        int scaledWidth = (int) (originalImage.getWidth() * scale);
        int scaledHeight = (int) (originalImage.getHeight() * scale);

        java.awt.image.BufferedImage thumbnail = new java.awt.image.BufferedImage(
                scaledWidth, scaledHeight, java.awt.image.BufferedImage.TYPE_INT_RGB
        );
        java.awt.Graphics2D g = thumbnail.createGraphics();
        g.setRenderingHint(
                java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(thumbnail, "jpg", baos);
        return baos.toByteArray();
    }
}
