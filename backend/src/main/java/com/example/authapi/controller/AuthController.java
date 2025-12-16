package com.example.authapi.controller;

import com.example.authapi.dto.AuthResponse;
import com.example.authapi.dto.LoginRequest;
import com.example.authapi.dto.RefreshTokenRequest;
import com.example.authapi.dto.RegisterRequest;
import com.example.authapi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * =====================================================================
 * AUTHENTICATION CONTROLLER
 * =====================================================================
 * 
 * REST API endpoints for authentication operations.
 * 
 * ENDPOINTS:
 * 
 * POST /api/auth/register  - Create new user account
 * POST /api/auth/login     - Authenticate and get tokens
 * POST /api/auth/refresh   - Get new access token using refresh token
 * 
 * KEY ANNOTATIONS:
 * 
 * @RestController = @Controller + @ResponseBody
 * - @Controller: Marks this as a Spring MVC controller
 * - @ResponseBody: Return values are serialized to JSON
 * 
 * @RequestMapping("/api/auth")
 * - Base path for all endpoints in this controller
 * 
 * @Valid
 * - Triggers validation of request body (checks @NotBlank, @Email, etc.)
 * - If validation fails, MethodArgumentNotValidException is thrown
 * 
 * @RequestBody
 * - Deserializes JSON request body into Java object
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user
     * 
     * POST /api/auth/register
     * 
     * Request Body:
     * {
     *   "username": "john",
     *   "email": "john@example.com",
     *   "password": "secret123"
     * }
     * 
     * Response (201 Created):
     * {
     *   "token": "eyJhbGciOiJIUzUxMiJ9...",
     *   "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
     *   "type": "Bearer",
     *   "expiresIn": 900000,
     *   "username": "john",
     *   "email": "john@example.com",
     *   "roles": ["ROLE_USER"]
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        
        log.info("Registration request for username: {}", request.getUsername());
        
        AuthResponse response = authService.register(request);
        
        // Return 201 Created status for new resource creation
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Authenticate user and return tokens
     * 
     * POST /api/auth/login
     * 
     * Request Body:
     * {
     *   "username": "john",
     *   "password": "secret123"
     * }
     * 
     * Response (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzUxMiJ9...",
     *   "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
     *   "type": "Bearer",
     *   "expiresIn": 900000,
     *   "username": "john",
     *   "email": "john@example.com",
     *   "roles": ["ROLE_USER"]
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        
        log.info("Login request for username: {}", request.getUsername());
        
        AuthResponse response = authService.login(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token
     * 
     * POST /api/auth/refresh
     * 
     * Request Body:
     * {
     *   "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
     * }
     * 
     * Response (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzUxMiJ9...",
     *   "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
     *   "type": "Bearer",
     *   "expiresIn": 900000,
     *   ...
     * }
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        
        log.info("Token refresh request");
        
        AuthResponse response = authService.refreshToken(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     * 
     * GET /api/auth/health
     * 
     * Useful for load balancers and monitoring.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "auth-api"
        ));
    }
}
