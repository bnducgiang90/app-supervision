package com.klkt.supervision.controller;

import com.klkt.supervision.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
@org.springframework.web.bind.annotation.CrossOrigin(origins = "*")
public class FileController {
    
    private final StorageProperties storageProperties;
    
    /**
     * Serve files from /api/files/** path
     */
    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public Mono<ResponseEntity<Resource>> serveFile(ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            String requestPath = exchange.getRequest().getPath().value();
            log.debug("Request path: {}", requestPath);
            
            // Remove /api/files prefix
            String filePath = requestPath.replaceFirst("^/api/files/?", "");
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }
            if (filePath.isEmpty()) {
                log.warn("Empty file path in request: {}", requestPath);
                return ResponseEntity.notFound().build();
            }
            
            Path rootLocation = Paths.get(storageProperties.getLocal().getUploadDir());
            Path file = rootLocation.resolve(filePath);
            Resource resource = new FileSystemResource(file);
            
            log.debug("Looking for file: {} (absolute: {})", filePath, file.toAbsolutePath());
            log.debug("File exists: {}, readable: {}", resource.exists(), resource.isReadable());
            
            if (resource.exists() && resource.isReadable()) {
                String filename = file.getFileName().toString();
                String contentType = determineContentType(filename);
                log.info("Serving file: {} -> {}", filePath, file.toAbsolutePath());
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                log.warn("File not found: {} (resolved to: {})", filePath, file.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
        });
    }
    
    private String determineContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".gif")) {
            return "image/gif";
        } else if (lower.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lower.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (lower.endsWith(".mov")) {
            return "video/quicktime";
        }
        return "application/octet-stream";
    }
}
