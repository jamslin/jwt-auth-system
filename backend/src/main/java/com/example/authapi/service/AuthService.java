package com.example.authapi.service;

import com.example.authapi.dto.AuthResponse;
import com.example.authapi.dto.LoginRequest;
import com.example.authapi.dto.RefreshTokenRequest;
import com.example.authapi.dto.RegisterRequest;
import com.example.authapi.entity.User;
import com.example.authapi.exception.AuthException;
import com.example.authapi.repository.UserRepository;
import com.example.authapi.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * =====================================================================
 * AUTHENTICATION SERVICE
 * =====================================================================
 * 
 * This service handles all authentication business logic:
 * - User registration
 * - User login
 * - Token refresh
 * 
 * AUTHENTICATION FLOW:
 * 
 * REGISTRATION:
 * 1. Validate request data (done by @Valid in controller)
 * 2. Check if username/email already exists
 * 3. Encode password with BCrypt
 * 4. Save user to database
 * 5. Generate and return JWT tokens
 * 
 * LOGIN:
 * 1. Authenticate using AuthenticationManager
 * 2. If successful, generate JWT tokens
 * 3. Return tokens and user info
 * 
 * TOKEN REFRESH:
 * 1. Validate refresh token
 * 2. Extract username from token
 * 3. Load user from database
 * 4. Generate new access token
 * 5. Return new tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user
     * 
     * @param request Registration details
     * @return AuthResponse with tokens
     * @throws AuthException if username or email already exists
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.debug("Registering new user: {}", request.getUsername());
        
        // =========================================================
        // STEP 1: Check if username already exists
        // =========================================================
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException("Username is already taken");
        }
        
        // =========================================================
        // STEP 2: Check if email already exists
        // =========================================================
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email is already registered");
        }
        
        // =========================================================
        // STEP 3: Create user with encoded password
        // =========================================================
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");  // Default role for new users
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))  // ENCODE!
                .roles(roles)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        
        // =========================================================
        // STEP 4: Save user to database
        // =========================================================
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());
        
        // =========================================================
        // STEP 5: Generate tokens and return response
        // =========================================================
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);
        
        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles())
                .build();
    }

    /**
     * Authenticate user and return tokens
     * 
     * @param request Login credentials
     * @return AuthResponse with tokens
     * @throws AuthException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        log.debug("Login attempt for user: {}", request.getUsername());
        
        try {
            // =========================================================
            // STEP 1: Authenticate using Spring Security
            // =========================================================
            // This will:
            // - Call UserDetailsService.loadUserByUsername()
            // - Verify password using PasswordEncoder
            // - Throw exception if authentication fails
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
            
            // =========================================================
            // STEP 2: Get authenticated user
            // =========================================================
            User user = (User) authentication.getPrincipal();
            log.info("User logged in successfully: {}", user.getUsername());
            
            // =========================================================
            // STEP 3: Generate tokens
            // =========================================================
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            // =========================================================
            // STEP 4: Return response
            // =========================================================
            return AuthResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .type("Bearer")
                    .expiresIn(jwtService.getAccessTokenExpiration())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(user.getRoles())
                    .build();
                    
        } catch (AuthenticationException e) {
            log.warn("Login failed for user {}: {}", request.getUsername(), e.getMessage());
            throw new AuthException("Invalid username or password");
        }
    }

    /**
     * Refresh access token using refresh token
     * 
     * @param request Refresh token
     * @return AuthResponse with new tokens
     * @throws AuthException if refresh token is invalid
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.debug("Token refresh request");
        
        try {
            // =========================================================
            // STEP 1: Extract username from refresh token
            // =========================================================
            String username = jwtService.extractUsername(request.getRefreshToken());
            
            // =========================================================
            // STEP 2: Load user from database
            // =========================================================
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AuthException("User not found"));
            
            // =========================================================
            // STEP 3: Validate refresh token
            // =========================================================
            if (!jwtService.isTokenValid(request.getRefreshToken(), user)) {
                throw new AuthException("Invalid refresh token");
            }
            
            // =========================================================
            // STEP 4: Generate new tokens
            // =========================================================
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            log.info("Token refreshed for user: {}", username);
            
            // =========================================================
            // STEP 5: Return new tokens
            // =========================================================
            return AuthResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .type("Bearer")
                    .expiresIn(jwtService.getAccessTokenExpiration())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(user.getRoles())
                    .build();
                    
        } catch (Exception e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            throw new AuthException("Invalid or expired refresh token");
        }
    }
}
