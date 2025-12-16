package com.example.authapi.config;

import com.example.authapi.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * =====================================================================
 * SECURITY CONFIGURATION
 * =====================================================================
 * 
 * This is the central configuration for Spring Security.
 * Here we define:
 * - Which endpoints are public vs protected
 * - How passwords are encoded
 * - How authentication works
 * - CORS settings
 * - Session management (stateless for JWT)
 * 
 * SECURITY FILTER CHAIN:
 * 
 * Every HTTP request passes through a chain of security filters:
 * 
 * Request → [CORS] → [CSRF] → [JWT Filter] → [Authorization] → Controller
 * 
 * @Configuration marks this as a configuration class
 * @EnableWebSecurity enables Spring Security
 * @EnableMethodSecurity enables @PreAuthorize, @Secured annotations
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enables method-level security annotations
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Security Filter Chain Configuration
     * 
     * This is the MAIN security configuration. It defines the security
     * rules for all HTTP requests.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        http
            // =========================================================
            // CORS Configuration
            // =========================================================
            // Enable CORS with our custom configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // =========================================================
            // CSRF Protection
            // =========================================================
            // Disable CSRF for REST APIs (JWT provides protection)
            // CSRF is mainly for session-based auth with cookies
            .csrf(AbstractHttpConfigurer::disable)
            
            // =========================================================
            // Authorization Rules
            // =========================================================
            .authorizeHttpRequests(auth -> auth
                // PUBLIC ENDPOINTS - No authentication required
                .requestMatchers(
                    "/api/auth/**",           // Login, register, refresh
                    "/api/public/**",         // Public API endpoints
                    "/h2-console/**",         // H2 database console
                    "/swagger-ui/**",         // Swagger UI (if added)
                    "/v3/api-docs/**",        // OpenAPI docs (if added)
                    "/error"                  // Error endpoint
                ).permitAll()
                
                // ADMIN ONLY ENDPOINTS
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // USER OR ADMIN ENDPOINTS
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                
                // ALL OTHER ENDPOINTS - Require authentication
                .anyRequest().authenticated()
            )
            
            // =========================================================
            // Session Management
            // =========================================================
            // STATELESS = No session will be created or used
            // Essential for JWT-based authentication!
            // Each request must include the JWT token.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // =========================================================
            // Authentication Provider
            // =========================================================
            .authenticationProvider(authenticationProvider())
            
            // =========================================================
            // JWT Filter
            // =========================================================
            // Add our JWT filter BEFORE the standard username/password filter
            .addFilterBefore(
                jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class
            )
            
            // =========================================================
            // H2 Console Frame Options
            // =========================================================
            // Allow frames for H2 console (development only!)
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }

    /**
     * Password Encoder
     * 
     * BCrypt is the industry standard for password hashing.
     * It automatically handles:
     * - Salt generation (random data added to password)
     * - Multiple rounds of hashing (configurable strength)
     * - Secure comparison (timing-attack resistant)
     * 
     * NEVER store plain text passwords!
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Provider
     * 
     * DaoAuthenticationProvider:
     * - Uses UserDetailsService to load user from database
     * - Uses PasswordEncoder to verify password
     * 
     * This is what actually performs the authentication logic.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        
        // Set our custom UserDetailsService
        authProvider.setUserDetailsService(userDetailsService);
        
        // Set the password encoder
        authProvider.setPasswordEncoder(passwordEncoder());
        
        return authProvider;
    }

    /**
     * Authentication Manager
     * 
     * The AuthenticationManager is the entry point for authentication.
     * It delegates to AuthenticationProvider(s) to perform the actual auth.
     * 
     * We expose it as a bean so we can inject it into our AuthService.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS Configuration
     * 
     * Cross-Origin Resource Sharing (CORS) settings for allowing
     * requests from different domains (like your frontend).
     * 
     * IMPORTANT: In production, restrict allowedOrigins to your
     * actual frontend domains!
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allowed origins (frontend URLs)
        // For development, allowing all origins. Restrict in production!
        configuration.setAllowedOrigins(List.of("*"));
        // Or be specific: configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        
        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin"
        ));
        
        // Exposed headers (headers the browser can access)
        configuration.setExposedHeaders(List.of("Authorization"));
        
        // Allow credentials (cookies, authorization headers)
        // Note: When allowedOrigins is "*", allowCredentials must be false
        configuration.setAllowCredentials(false);
        
        // How long the browser should cache CORS configuration (in seconds)
        configuration.setMaxAge(3600L);
        
        // Apply configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
