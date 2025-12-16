package com.example.authapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * =====================================================================
 * JWT AUTHENTICATION FILTER
 * =====================================================================
 * 
 * This filter intercepts EVERY incoming HTTP request to check for a
 * valid JWT token in the Authorization header.
 * 
 * FILTER CHAIN FLOW:
 * 
 * Client Request
 *      ↓
 * [JwtAuthenticationFilter] ← We're here!
 *      ↓
 * [Other Security Filters]
 *      ↓
 * [Controller/Endpoint]
 *      ↓
 * Response to Client
 * 
 * OncePerRequestFilter ensures this filter is only executed once per request
 * (important when there are multiple servlet dispatches).
 * 
 * AUTHENTICATION FLOW:
 * 
 * 1. Extract JWT from "Authorization: Bearer <token>" header
 * 2. Validate the token signature and expiration
 * 3. Extract username from token
 * 4. Load user details from database
 * 5. Create Authentication object and set in SecurityContext
 * 6. Continue filter chain (request proceeds to controller)
 */
@Component
@RequiredArgsConstructor  // Lombok: Constructor injection for final fields
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Main filter method - called for every request
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // =====================================================
        // STEP 1: Extract JWT from Authorization Header
        // =====================================================
        final String jwt = extractJwtFromRequest(request);
        
        // If no JWT found, continue to next filter
        // (might be a public endpoint that doesn't require auth)
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // =====================================================
            // STEP 2: Extract Username from Token
            // =====================================================
            final String username = jwtService.extractUsername(jwt);
            
            // =====================================================
            // STEP 3: Check if User is Not Already Authenticated
            // =====================================================
            // SecurityContextHolder.getContext().getAuthentication() returns
            // the current authentication or null if not authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // =====================================================
                // STEP 4: Load User from Database
                // =====================================================
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                // =====================================================
                // STEP 5: Validate Token
                // =====================================================
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    
                    // =====================================================
                    // STEP 6: Create Authentication Token
                    // =====================================================
                    // UsernamePasswordAuthenticationToken represents an
                    // authenticated user. Three-arg constructor marks it
                    // as authenticated.
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails,           // Principal (the user)
                            null,                  // Credentials (null, we use JWT)
                            userDetails.getAuthorities()  // Roles/permissions
                        );
                    
                    // Add request details (IP, session ID, etc.)
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // =====================================================
                    // STEP 7: Set Authentication in Security Context
                    // =====================================================
                    // This is the KEY step! After this, Spring Security
                    // considers the user authenticated for this request.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.debug("User '{}' authenticated successfully", username);
                }
            }
        } catch (Exception e) {
            // Log error but don't stop the filter chain
            // The request will be rejected later if auth is required
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        // =====================================================
        // STEP 8: Continue Filter Chain
        // =====================================================
        // Pass request to next filter in the chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from the Authorization header
     * 
     * Expected format: "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
     * 
     * @param request The HTTP request
     * @return JWT token string, or null if not found
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        // Get the Authorization header
        String bearerToken = request.getHeader("Authorization");
        
        // Check if header exists and starts with "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extract token (everything after "Bearer ")
            return bearerToken.substring(7);
        }
        
        return null;
    }
}
