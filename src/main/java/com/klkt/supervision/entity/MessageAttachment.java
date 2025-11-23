package com.klkt.supervision.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("message_attachments")
public class MessageAttachment {
    
    @Id
    private Long id;
    
    private Long messageId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
    private String storagePath;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
}
