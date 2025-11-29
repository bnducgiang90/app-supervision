package com.klkt.supervision.controller;

import com.klkt.supervision.dto.MessageResponse;
import com.klkt.supervision.dto.SendMessageRequest;
import com.klkt.supervision.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;
    private final ObjectMapper objectMapper;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        return messageService.sendMessage(request);
    }
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MessageResponse> sendMessageWithFiles(
            @RequestPart("message") String messageJson,
            @RequestPart(value = "files", required = false) Flux<FilePart> files) {
        
        log.info("=== Received upload request ===");
        log.info("Message JSON (raw): {}", messageJson);
        log.info("Message JSON length: {}", messageJson != null ? messageJson.length() : 0);
        
        return Mono.fromCallable(() -> {
                    log.info("Parsing JSON string to SendMessageRequest...");
                    SendMessageRequest request = objectMapper.readValue(messageJson, SendMessageRequest.class);
                    log.info("Parsed successfully. InfoData is null: {}", request.getInfoData() == null);
                    log.info("=== Parsed SendMessageRequest ===");
                    log.info("GroupId: {}", request.getGroupId());
                    log.info("SenderId: {}", request.getSenderId());
                    log.info("Content: {}", request.getContent());
                    log.info("MessageType: {}", request.getMessageType());
                    if (request.getInfoData() != null) {
                        log.info("InfoData: {}", request.getInfoData());
                        Object locationObj = request.getInfoData().get("location");
                        if (locationObj instanceof java.util.Map) {
                            @SuppressWarnings("unchecked")
                            java.util.Map<String, Object> location = (java.util.Map<String, Object>) locationObj;
                            log.info("Location - Latitude: {}", location.get("latitude"));
                            log.info("Location - Longitude: {}", location.get("longitude"));
                            log.info("Location - LocationDetail: {}", location.get("locationDetail"));
                        }
                    } else {
                        log.info("InfoData is null");
                    }
                    return request;
                })
                .flatMap(request -> 
                        files != null 
                            ? files.collectList().flatMap(fileList -> {
                                    log.info("Processing {} files with attachments", fileList.size());
                                    return messageService.sendMessageWithAttachments(request, fileList);
                                })
                            : messageService.sendMessage(request))
                .doOnError(e -> log.error("Error sending message with files", e));
    }



    @GetMapping("/group/{groupId}")
    public Flux<MessageResponse> getGroupMessages(
            @PathVariable Long groupId,
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "50") Integer size) {
        return messageService.getGroupMessages(groupId, userId, page, size);
    }
    
    @GetMapping("/{messageId}")
    public Mono<MessageResponse> getMessageById(@PathVariable Long messageId) {
        return messageService.getMessageById(messageId);
    }

//
//    @PostMapping("/upload/image")
//    public ResponseEntity<Map<String, Object>> uploadImage(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam String senderId,
//            @RequestParam String senderName,
//            @RequestParam(required = false) String caption) {
//
//        try {
//            if (!localStorageService.isImageFile(file.getOriginalFilename())) {
//                return ResponseEntity.badRequest()
//                        .body(Map.of("error", "File is not a valid image"));
//            }
//
//            String filePath = storageService.storeFile(file, "image");
//            String fileUrl = storageService.getFileUrl(filePath);
//
//            String thumbnailUrl = null;
//            try {
//                if (storageService.getStorageType().equals("LOCAL")) {
//                    byte[] thumbnailData = localStorageService.generateThumbnail(filePath);
//                    String thumbnailPath = storageService.createThumbnail(filePath, thumbnailData);
//                    thumbnailUrl = storageService.getFileUrl(thumbnailPath);
//                } else {
//                    byte[] thumbnailData = generateThumbnailFromStream(file.getInputStream());
//                    String thumbnailPath = storageService.createThumbnail(filePath, thumbnailData);
//                    thumbnailUrl = storageService.getFileUrl(thumbnailPath);
//                }
//            } catch (Exception e) {
//                System.err.println("Failed to create thumbnail: " + e.getMessage());
//            }
//
//            ChatMessage message = new ChatMessage();
//            message.setId(UUID.randomUUID().toString());
//            message.setSenderId(senderId);
//            message.setSenderName(senderName);
//            message.setType(ChatMessage.MessageType.IMAGE);
//            message.setContent(caption != null ? caption : "");
//            message.setFileUrl(fileUrl);
//            message.setFileName(file.getOriginalFilename());
//            message.setFileSize(file.getSize());
//            message.setThumbnailUrl(thumbnailUrl);
//
//            sseService.broadcastMessage(message);
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("status", "success");
//            response.put("messageId", message.getId());
//            response.put("fileUrl", fileUrl);
//            response.put("thumbnailUrl", thumbnailUrl);
//            response.put("storageType", storageService.getStorageType());
//            response.put("mode", "SSE_EMITTER");
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
//        }
//    }
//
//    @PostMapping("/upload/video")
//    public ResponseEntity<Map<String, Object>> uploadVideo(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam String senderId,
//            @RequestParam String senderName,
//            @RequestParam(required = false) String caption) {
//
//        try {
//            if (!localStorageService.isVideoFile(file.getOriginalFilename())) {
//                return ResponseEntity.badRequest()
//                        .body(Map.of("error", "File is not a valid video"));
//            }
//
//            String filePath = storageService.storeFile(file, "video");
//            String fileUrl = storageService.getFileUrl(filePath);
//
//            ChatMessage message = new ChatMessage();
//            message.setId(UUID.randomUUID().toString());
//            message.setSenderId(senderId);
//            message.setSenderName(senderName);
//            message.setType(ChatMessage.MessageType.VIDEO);
//            message.setContent(caption != null ? caption : "");
//            message.setFileUrl(fileUrl);
//            message.setFileName(file.getOriginalFilename());
//            message.setFileSize(file.getSize());
//
//            sseService.broadcastMessage(message);
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("status", "success");
//            response.put("messageId", message.getId());
//            response.put("fileUrl", fileUrl);
//            response.put("storageType", storageService.getStorageType());
//            response.put("mode", "SSE_EMITTER");
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Failed to upload video: " + e.getMessage()));
//        }
//    }


    private byte[] generateThumbnailFromStream(java.io.InputStream inputStream) throws Exception {
        java.awt.image.BufferedImage originalImage = javax.imageio.ImageIO.read(inputStream);
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
