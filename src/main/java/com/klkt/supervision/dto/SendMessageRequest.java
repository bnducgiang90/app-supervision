package com.klkt.supervision.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    
    @NotNull(message = "Group ID is required")
    private Long groupId;
    
    @NotNull(message = "Sender ID is required")
    private Long senderId;
    
    private String content;
    private String messageType = "TEXT";
    
    // Location information for images/videos
    private Double latitude;
    private Double longitude;
    private String locationDetail; // Detailed coordinate information
}
