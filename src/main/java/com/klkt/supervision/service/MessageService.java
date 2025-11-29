package com.klkt.supervision.service;

import com.klkt.supervision.dto.AttachmentResponse;
import com.klkt.supervision.dto.MessageResponse;
import com.klkt.supervision.dto.SendMessageRequest;
import com.klkt.supervision.entity.Message;
import com.klkt.supervision.entity.MessageAttachment;
import com.klkt.supervision.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final MessageAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final FileStorageService fileStorageService;
    private final SSEService sseService;
    
    public Mono<MessageResponse> sendMessage(SendMessageRequest request) {
        return validateUserInGroup(request.getSenderId(), request.getGroupId())
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(new RuntimeException(
                                "User is not a member of this group"));
                    }
                    
                    // Get group to retrieve group_code
                    return groupRepository.findById(request.getGroupId())
                            .switchIfEmpty(Mono.error(new RuntimeException("Group not found")))
                            .flatMap(group -> {
                                Message message = Message.builder()
                                        .groupId(request.getGroupId())
                                        .groupCode(group.getGroupCode())
                                        .senderId(request.getSenderId())
                                        .content(request.getContent())
                                        .messageType(Message.MessageType.valueOf(
                                                request.getMessageType().toUpperCase()))
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                                
                                return messageRepository.save(message)
                                        .flatMap(savedMessage -> 
                                                buildMessageResponse(savedMessage, List.of()))
                                        .doOnSuccess(msgResponse -> {
                                            log.info("Message sent to group {} (code: {}): {}", 
                                                    request.getGroupId(), group.getGroupCode(), msgResponse.getId());
                                            sseService.sendToGroup(request.getGroupId(), 
                                                    "new_message", msgResponse);
                                        });
                            });
                });
    }
    
    public Mono<MessageResponse> sendMessageWithAttachments(
            SendMessageRequest request, 
            List<FilePart> files) {
        
        return validateUserInGroup(request.getSenderId(), request.getGroupId())
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(new RuntimeException(
                                "User is not a member of this group"));
                    }
                    
                    // Get group to retrieve group_code
                    return groupRepository.findById(request.getGroupId())
                            .switchIfEmpty(Mono.error(new RuntimeException("Group not found")))
                            .flatMap(group -> {
                                // Determine message type from files
                                String messageType = determineMessageType(files);
                                
                                // Log location data from request
                                log.info("=== Location Data from Request ===");
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
                                
                                Message message = Message.builder()
                                        .groupId(request.getGroupId())
                                        .groupCode(group.getGroupCode())
                                        .senderId(request.getSenderId())
                                        .content(request.getContent())
                                        .messageType(Message.MessageType.valueOf(messageType))
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                                
                                return messageRepository.save(message)
                                        .flatMap(savedMessage -> {
                                                log.info("Saved message with id: {}", savedMessage.getId());
                                                // Extract location data from infoData Map
                                                Double latitude = null;
                                                Double longitude = null;
                                                String locationDetail = null;
                                                if (request.getInfoData() != null) {
                                                    Object locationObj = request.getInfoData().get("location");
                                                    if (locationObj instanceof java.util.Map) {
                                                        @SuppressWarnings("unchecked")
                                                        java.util.Map<String, Object> location = (java.util.Map<String, Object>) locationObj;
                                                        Object latObj = location.get("latitude");
                                                        Object longObj = location.get("longitude");
                                                        Object detailObj = location.get("locationDetail");
                                                        
                                                        if (latObj instanceof Number) {
                                                            latitude = ((Number) latObj).doubleValue();
                                                        }
                                                        if (longObj instanceof Number) {
                                                            longitude = ((Number) longObj).doubleValue();
                                                        }
                                                        if (detailObj instanceof String) {
                                                            locationDetail = (String) detailObj;
                                                        }
                                                    }
                                                }
                                                return uploadAttachments(savedMessage.getId(), savedMessage.getGroupCode(), files, 
                                                        latitude, longitude, locationDetail)
                                                        .collectList()
                                                        .flatMap(attachments -> {
                                                            // Store additional metadata in infoData if needed (optional)
                                                            if (!attachments.isEmpty()) {
                                                                java.util.Map<String, Object> infoMap = new java.util.HashMap<>();
                                                                infoMap.put("attachmentCount", attachments.size());
                                                                
                                                                try {
                                                                    com.fasterxml.jackson.databind.ObjectMapper mapper = 
                                                                            new com.fasterxml.jackson.databind.ObjectMapper();
                                                                    savedMessage.setInfoData(mapper.writeValueAsString(infoMap));
                                                                    return messageRepository.save(savedMessage)
                                                                            .flatMap(updatedMessage -> 
                                                                                    buildMessageResponse(updatedMessage, attachments));
                                                                } catch (Exception e) {
                                                                    log.warn("Failed to serialize info data", e);
                                                                    return buildMessageResponse(savedMessage, attachments);
                                                                }
                                                            } else {
                                                                return buildMessageResponse(savedMessage, attachments);
                                                            }
                                                        })
                                                        .doOnSuccess(msgResponse -> {
                                                            // Broadcast SSE event after message is successfully created and attachments uploaded
                                                            log.info("Message with {} attachments sent to group {} (code: {}), broadcasting SSE event", 
                                                                    files.size(), request.getGroupId(), group.getGroupCode());
                                                            if (msgResponse != null) {
                                                                sseService.sendToGroup(request.getGroupId(), 
                                                                        "new_message", msgResponse);
                                                            } else {
                                                                log.warn("Message response is null, cannot broadcast SSE event");
                                                            }
                                                        })
                                                        .doOnError(error -> {
                                                            log.error("Error processing message with attachments for group {}", 
                                                                    request.getGroupId(), error);
                                                        });
                                        });
                            });
                });
    }
    
    public Flux<MessageResponse> getGroupMessages(Long groupId, Long userId, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(
                page != null ? page : 0, 
                size != null ? size : 50
        );
        
        // Validate user has access to this group (ADMIN or member)
        return validateUserInGroup(userId, groupId)
                .flatMapMany(hasAccess -> {
                    if (!hasAccess) {
                        return Flux.error(new RuntimeException(
                                "User does not have access to this group"));
                    }
                    
                    return messageRepository.findByGroupIdOrderByCreatedAtDesc(groupId, pageRequest)
                            .flatMap(message -> 
                                    attachmentRepository.findByMessageId(message.getId())
                                            .collectList()
                                            .flatMap(attachments -> 
                                                    buildMessageResponse(message, attachments)));
                });
    }
    
    public Mono<MessageResponse> getMessageById(Long messageId) {
        return messageRepository.findById(messageId)
                .flatMap(message -> 
                        attachmentRepository.findByMessageId(message.getId())
                                .collectList()
                                .flatMap(attachments -> 
                                        buildMessageResponse(message, attachments)))
                .switchIfEmpty(Mono.error(new RuntimeException("Message not found")));
    }
    
    private Mono<Boolean> validateUserInGroup(Long userId, Long groupId) {
        // First check if user is ADMIN - if so, allow access to all groups
        return userRepository.findById(userId)
                .flatMap(user -> {
                    if (user.getRole() != null && user.getRole() == com.klkt.supervision.entity.User.UserRole.ADMIN) {
                        return Mono.just(true);
                    }
                    // If not ADMIN, check if user is a member of the group
                    return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
                })
                .switchIfEmpty(Mono.just(false));
    }
    
    private Flux<MessageAttachment> uploadAttachments(Long messageId, String groupCode, List<FilePart> files,
                                                      Double latitude, Double longitude, String locationDetail) {
        // Log received parameters
        log.info("=== uploadAttachments called ===");
        log.info("messageId: {}", messageId);
        log.info("groupCode: {}", groupCode);
        log.info("files count: {}", files != null ? files.size() : 0);
        log.info("latitude: {}", latitude);
        log.info("longitude: {}", longitude);
        log.info("locationDetail: {}", locationDetail);
        
        // Get current date in yyyy-MM-dd format
        String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Default location: Hà Nội, Việt Nam
        final double DEFAULT_LATITUDE = 21.0285;
        final double DEFAULT_LONGITUDE = 105.8542;
        final String DEFAULT_LOCATION_DETAIL = "Hà Nội, Việt Nam";
        
        // Use default values if location data is null
        double finalLatitude = latitude != null ? latitude : DEFAULT_LATITUDE;
        double finalLongitude = longitude != null ? longitude : DEFAULT_LONGITUDE;
        String finalLocationDetail = (locationDetail != null && !locationDetail.isEmpty()) 
                ? locationDetail : DEFAULT_LOCATION_DETAIL;
        
        log.info("Checking location data - lat: {}, long: {}, detail: {}", latitude, longitude, locationDetail);
        log.info("Using location - lat: {}, long: {}, detail: {}", finalLatitude, finalLongitude, finalLocationDetail);
        
        // Build location info JSON (always create JSON with location data)
        // Format: {"location":{"detail":"Hà Nội, Việt Nam","lat":21.0285,"long":105.8542}}
        String infoDataJson = null;
        try {
            java.util.Map<String, Object> locationMap = new java.util.HashMap<>();
            locationMap.put("lat", finalLatitude);
            locationMap.put("long", finalLongitude);
            locationMap.put("detail", finalLocationDetail);
            
            java.util.Map<String, Object> rootMap = new java.util.HashMap<>();
            rootMap.put("location", locationMap);
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = 
                    new com.fasterxml.jackson.databind.ObjectMapper();
            infoDataJson = mapper.writeValueAsString(rootMap);
            log.info("Location JSON created successfully: {}", infoDataJson);
        } catch (Exception e) {
            log.error("Failed to serialize location data", e);
        }
        
        final String finalInfoDataJson = infoDataJson;
        log.info("Final infoDataJson value: {}", finalInfoDataJson);
        
        return Flux.fromIterable(files)
                .flatMap(file -> {
                    // New folder structure: group_code/messages/messageId/yyyy-MM-dd
                    String folder = groupCode + "/messages/" + messageId + "/" + dateFolder;
                    return fileStorageService.uploadFile(file, folder)
                            .flatMap(uploadResponse -> {
                                log.info("Building MessageAttachment for file: {}", uploadResponse.getFileName());
                                log.info("infoData value before building: {}", finalInfoDataJson);
                                
                                MessageAttachment attachment = MessageAttachment.builder()
                                        .messageId(messageId)
                                        .fileName(uploadResponse.getFileName())
                                        .fileType(uploadResponse.getFileType())
                                        .fileSize(uploadResponse.getFileSize())
                                        .fileUrl(uploadResponse.getFileUrl())
                                        .storagePath(uploadResponse.getStoragePath())
                                        .thumbnailUrl(uploadResponse.getThumbnailUrl())
                                        .createdAt(LocalDateTime.now())
                                        .infoData(finalInfoDataJson)
                                        .build();
                                
                                log.info("=== MessageAttachment object created ===");
                                log.info("Attachment ID: {}", attachment.getId());
                                log.info("Message ID: {}", attachment.getMessageId());
                                log.info("File Name: {}", attachment.getFileName());
                                log.info("File Type: {}", attachment.getFileType());
                                log.info("infoData: {}", attachment.getInfoData());
                                log.info("infoData is null: {}", attachment.getInfoData() == null);
                                
                                return attachmentRepository.save(attachment)
                                        .doOnSuccess(saved -> {
                                            log.info("=== MessageAttachment saved successfully ===");
                                            log.info("Saved Attachment ID: {}", saved.getId());
                                            log.info("Saved infoData: {}", saved.getInfoData());
                                            log.info("Saved infoData is null: {}", saved.getInfoData() == null);
                                        })
                                        .doOnError(error -> {
                                            log.error("Error saving MessageAttachment", error);
                                        });
                            });
                });
    }
    
    private Mono<MessageResponse> buildMessageResponse(
            Message message, 
            List<MessageAttachment> attachments) {
        
        return userRepository.findById(message.getSenderId())
                .map(sender -> {
                    List<AttachmentResponse> attachmentResponses = attachments.stream()
                            .map(att -> AttachmentResponse.builder()
                                    .id(att.getId())
                                    .fileName(att.getFileName())
                                    .fileType(att.getFileType())
                                    .fileSize(att.getFileSize())
                                    .fileUrl(att.getFileUrl())
                                    .thumbnailUrl(att.getThumbnailUrl())
                                    .infoData(att.getInfoData())
                                    .build())
                            .toList();
                    
                    return MessageResponse.builder()
                            .id(message.getId())
                            .groupId(message.getGroupId())
                            .senderId(message.getSenderId())
                            .senderName(sender.getDisplayName())
                            .senderAvatar(sender.getAvatarUrl())
                            .content(message.getContent())
                            .messageType(message.getMessageType().name())
                            .attachments(attachmentResponses)
                            .createdAt(message.getCreatedAt())
                            .build();
                })
                .defaultIfEmpty(MessageResponse.builder()
                        .id(message.getId())
                        .groupId(message.getGroupId())
                        .senderId(message.getSenderId())
                        .senderName("Unknown User")
                        .content(message.getContent())
                        .messageType(message.getMessageType().name())
                        .attachments(List.of())
                        .createdAt(message.getCreatedAt())
                        .build());
    }
    
    
    private String determineMessageType(List<FilePart> files) {
        if (files == null || files.isEmpty()) {
            return "TEXT";
        }
        
        String firstFileName = files.get(0).filename().toLowerCase();
        if (firstFileName.matches(".*\\.(jpg|jpeg|png|gif|webp)$")) {
            return "IMAGE";
        } else if (firstFileName.matches(".*\\.(mp4|avi|mov|wmv|flv)$")) {
            return "VIDEO";
        }
        
        return "FILE";
    }
}
