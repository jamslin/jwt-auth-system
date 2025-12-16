package com.example.authapi.controller;

import com.example.authapi.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * =====================================================================
 * TEST CONTROLLER
 * =====================================================================
 * 
 * This controller demonstrates different access levels:
 * - Public endpoints (no authentication required)
 * - Protected endpoints (authentication required)
 * - Role-based endpoints (specific roles required)
 * 
 * ACCESS LEVELS IN OUR SECURITY CONFIG:
 * 
 * PUBLIC:     /api/auth/**, /api/public/**
 * USER/ADMIN: /api/user/**
 * ADMIN ONLY: /api/admin/**
 * OTHER:      Requires authentication
 * 
 * GETTING CURRENT USER:
 * 
 * Method 1: @AuthenticationPrincipal annotation
 *           Injects the UserDetails (our User entity)
 * 
 * Method 2: Authentication parameter
 *           Full authentication object with more details
 * 
 * Method 3: SecurityContextHolder.getContext().getAuthentication()
 *           Programmatic access (useful in services)
 */
@RestController
@RequiredArgsConstructor
public class TestController {

    // =================================================================
    // PUBLIC ENDPOINTS
    // =================================================================

    /**
     * Public endpoint - accessible without authentication
     * 
     * GET /api/public/hello
     */
    @GetMapping("/api/public/hello")
    public ResponseEntity<Map<String, Object>> publicHello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from public endpoint!");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("authenticated", false);
        return ResponseEntity.ok(response);
    }

    // =================================================================
    // PROTECTED ENDPOINTS (Requires Authentication)
    // =================================================================

    /**
     * Protected endpoint - requires valid JWT token
     * 
     * GET /api/test
     * 
     * Headers:
     *   Authorization: Bearer <your-jwt-token>
     */
    @GetMapping("/api/test")
    public ResponseEntity<Map<String, Object>> protectedTest(
            @AuthenticationPrincipal User user) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from protected endpoint!");
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("roles", user.getRoles());
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user profile
     * 
     * GET /api/profile
     * 
     * Demonstrates using Authentication object
     */
    @GetMapping("/api/profile")
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("roles", user.getRoles());
        response.put("enabled", user.isEnabled());
        response.put("createdAt", user.getCreatedAt());
        
        return ResponseEntity.ok(response);
    }

    // =================================================================
    // USER ROLE ENDPOINTS
    // =================================================================

    /**
     * User endpoint - requires ROLE_USER or ROLE_ADMIN
     * 
     * GET /api/user/dashboard
     * 
     * Configured in SecurityConfig: .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
     */
    @GetMapping("/api/user/dashboard")
    public ResponseEntity<Map<String, Object>> userDashboard(
            @AuthenticationPrincipal User user) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to your dashboard!");
        response.put("username", user.getUsername());
        response.put("accessLevel", "USER");
        response.put("features", new String[]{"View Profile", "Edit Settings", "View Data"});
        
        return ResponseEntity.ok(response);
    }

    // =================================================================
    // ADMIN ROLE ENDPOINTS
    // =================================================================

    /**
     * Admin endpoint - requires ROLE_ADMIN only
     * 
     * GET /api/admin/dashboard
     * 
     * Configured in SecurityConfig: .requestMatchers("/api/admin/**").hasRole("ADMIN")
     * 
     * If user doesn't have ADMIN role, returns 403 Forbidden
     */
    @GetMapping("/api/admin/dashboard")
    public ResponseEntity<Map<String, Object>> adminDashboard(
            @AuthenticationPrincipal User user) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Admin Dashboard!");
        response.put("username", user.getUsername());
        response.put("accessLevel", "ADMIN");
        response.put("features", new String[]{
            "Manage Users", 
            "View All Data", 
            "System Settings",
            "Audit Logs"
        });
        
        return ResponseEntity.ok(response);
    }

    /**
     * Alternative: Using @PreAuthorize annotation
     * 
     * GET /api/admin/users
     * 
     * @PreAuthorize is evaluated before method execution
     * Requires @EnableMethodSecurity in SecurityConfig
     */
    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")  // Method-level security
    public ResponseEntity<Map<String, Object>> getUsers() {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User list (admin only)");
        response.put("users", new String[]{"user1", "user2", "user3"});
        
        return ResponseEntity.ok(response);
    }

    /**
     * Complex authorization with @PreAuthorize
     * 
     * Only accessible if user is admin OR accessing their own data
     */
    @GetMapping("/api/user/{userId}/data")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<Map<String, Object>> getUserData(
            Long userId,
            @AuthenticationPrincipal User user) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("requestedBy", user.getUsername());
        response.put("data", "Sensitive user data here");
        
        return ResponseEntity.ok(response);
    }
}
