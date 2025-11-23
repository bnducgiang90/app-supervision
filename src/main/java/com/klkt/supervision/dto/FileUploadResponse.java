package com.klkt.supervision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    
    private String fileName;
    private String fileUrl;
    private String storagePath;
    private Long fileSize;
    private String fileType;
    private String thumbnailUrl;
}
