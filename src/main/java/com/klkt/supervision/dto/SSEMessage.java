package com.klkt.supervision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSEMessage {
    
    private String eventType;
    private Object data;
    private LocalDateTime timestamp;
}
