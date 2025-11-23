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
@Table("messages")
public class Message {
    
    @Id
    private Long id;
    
    private Long groupId;
    private String groupCode;
    private Long senderId;
    private String content;
    private MessageType messageType;
    private String infoData; // JSON string for additional info (optional metadata)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum MessageType {
        TEXT, IMAGE, VIDEO, FILE
    }
}
