package com.example.authapi.config;

import com.example.authapi.entity.User;
import com.example.authapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * =====================================================================
 * DATA INITIALIZER
 * =====================================================================
 * 
 * This class creates test users when the application starts.
 * 
 * CommandLineRunner is executed after the Spring context is initialized.
 * Perfect for:
 * - Creating initial data
 * - Running database migrations
 * - Warming up caches
 * 
 * TEST ACCOUNTS CREATED:
 * 
 * | Username | Password | Role       |
 * |----------|----------|------------|
 * | user     | user123  | ROLE_USER  |
 * | admin    | admin123 | ROLE_ADMIN |
 * 
 * NOTE: In production, remove this or use environment-specific config
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("Initializing test data...");
            
            // Create regular user if not exists
            if (!userRepository.existsByUsername("user")) {
                User user = User.builder()
                        .username("user")
                        .email("user@example.com")
                        .password(passwordEncoder.encode("user123"))
                        .roles(Set.of("ROLE_USER"))
                        .enabled(true)
                        .accountNonExpired(true)
                        .accountNonLocked(true)
                        .credentialsNonExpired(true)
                        .build();
                
                userRepository.save(user);
                log.info("Created test user: user / user123");
            }
            
            // Create admin user if not exists
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("admin123"))
                        .roles(Set.of("ROLE_USER", "ROLE_ADMIN"))  // Admin has both roles
                        .enabled(true)
                        .accountNonExpired(true)
                        .accountNonLocked(true)
                        .credentialsNonExpired(true)
                        .build();
                
                userRepository.save(admin);
                log.info("Created test admin: admin / admin123");
            }
            
            log.info("Data initialization complete!");
            log.info("");
            log.info("=========================================");
            log.info("  Test Accounts:");
            log.info("  - User:  user / user123");
            log.info("  - Admin: admin / admin123");
            log.info("=========================================");
        };
    }
}
