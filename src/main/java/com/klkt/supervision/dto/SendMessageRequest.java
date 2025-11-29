package com.klkt.supervision.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

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
    
    // Flexible JSON data for additional information (location, etc.)
    // Format: {"location": {"latitude": 10.123456, "longitude": 106.654321, "locationDetail": "TP.HCM"}}
    // Can be extended with other fields in the future without changing DTO
    @JsonProperty("infoData")
    private Map<String, Object> infoData;
}
