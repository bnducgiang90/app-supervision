package com.klkt.supervision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
    private String thumbnailUrl;
    private String infoData; // JSON string containing location info (lat, long, detail)
}
