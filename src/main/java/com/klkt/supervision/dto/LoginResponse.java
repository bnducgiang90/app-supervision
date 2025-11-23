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
public class LoginResponse {
    
    private String token;
    private Long id;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}

