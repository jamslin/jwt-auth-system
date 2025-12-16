package com.example.authapi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * =====================================================================
 * JWT SERVICE
 * =====================================================================
 * 
 * This service handles all JWT token operations:
 * - Token generation (creating tokens)
 * - Token validation (checking if tokens are valid)
 * - Token parsing (extracting claims/data from tokens)
 * 
 * JWT STRUCTURE:
 * A JWT consists of three parts separated by dots (.):
 * 
 * Header.Payload.Signature
 * 
 * 1. HEADER (Algorithm & Token Type)
 *    {"alg": "HS512", "typ": "JWT"}
 *    
 * 2. PAYLOAD (Claims/Data)
 *    {
 *      "sub": "username",           // Subject (who the token is about)
 *      "iat": 1234567890,           // Issued At (when token was created)
 *      "exp": 1234567890,           // Expiration (when token expires)
 *      "roles": ["ROLE_USER"]       // Custom claims
 *    }
 *    
 * 3. SIGNATURE (Verification)
 *    HMACSHA512(base64(header) + "." + base64(payload), secret)
 *    
 * The signature ensures the token hasn't been tampered with.
 */
@Service
@Slf4j  // Lombok: Creates a logger field
public class JwtService {

    /**
     * Secret key for signing tokens
     * Loaded from application.yml: jwt.secret
     * 
     * IMPORTANT: In production:
     * - Use a long, random secret (256+ bits for HS512)
     * - Store in environment variable, not in code
     * - Never commit secrets to version control
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Access token expiration time in milliseconds
     * Short-lived for security (typically 15 min - 1 hour)
     */
    @Value("${jwt.expiration.access}")
    private long accessTokenExpiration;

    /**
     * Refresh token expiration time in milliseconds
     * Longer-lived (typically 7-30 days)
     * Used to get new access tokens without re-login
     */
    @Value("${jwt.expiration.refresh}")
    private long refreshTokenExpiration;

    // =================================================================
    // TOKEN GENERATION
    // =================================================================

    /**
     * Generate access token from Authentication object
     * Called after successful login
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateAccessToken(userDetails);
    }

    /**
     * Generate access token from UserDetails
     */
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, accessTokenExpiration);
    }

    /**
     * Generate refresh token
     * Contains minimal claims, just enough to identify user
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, refreshTokenExpiration);
    }

    /**
     * Core token generation method
     * 
     * @param extraClaims Additional data to include in token
     * @param userDetails User information
     * @param expiration  Token lifetime in milliseconds
     * @return Signed JWT string
     */
    private String generateToken(Map<String, Object> extraClaims, 
                                  UserDetails userDetails, 
                                  long expiration) {
        
        // Add roles to token claims
        extraClaims.put("roles", userDetails.getAuthorities());
        
        return Jwts.builder()
                // Set custom claims first (can be overwritten by standard claims)
                .claims(extraClaims)
                // Subject = username (who this token is for)
                .subject(userDetails.getUsername())
                // When the token was issued
                .issuedAt(new Date(System.currentTimeMillis()))
                // When the token expires
                .expiration(new Date(System.currentTimeMillis() + expiration))
                // Sign with our secret key using HS512 algorithm
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                // Build the JWT string
                .compact();
    }

    // =================================================================
    // TOKEN VALIDATION
    // =================================================================

    /**
     * Validate a token against user details
     * 
     * Checks:
     * 1. Token signature is valid
     * 2. Token is not expired
     * 3. Token username matches provided userDetails
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if token is expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // =================================================================
    // CLAIM EXTRACTION
    // =================================================================

    /**
     * Extract username (subject) from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract any claim
     * Uses functional interface for flexibility
     * 
     * @param token          The JWT token
     * @param claimsResolver Function that extracts the desired claim
     * @return The extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse token and extract all claims
     * 
     * This is where signature verification happens.
     * If signature is invalid, an exception is thrown.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // =================================================================
    // HELPER METHODS
    // =================================================================

    /**
     * Get the signing key from our secret
     * 
     * The secret string is Base64 decoded and converted to a SecretKey
     * This key is used for both signing (creating) and verifying tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get access token expiration time
     * Useful for including in response to client
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
}
