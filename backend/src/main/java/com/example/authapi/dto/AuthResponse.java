package com.example.authapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Response body for successful authentication
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    
    @Builder.Default
    private String type = "Bearer";
    
    private Long expiresIn;
    private String username;
    private String email;
    private Set<String> roles;
}
