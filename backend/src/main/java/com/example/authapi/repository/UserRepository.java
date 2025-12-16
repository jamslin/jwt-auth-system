package com.example.authapi.repository;

import com.example.authapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * =====================================================================
 * USER REPOSITORY
 * =====================================================================
 * 
 * This interface extends JpaRepository to provide database operations
 * for the User entity.
 * 
 * KEY CONCEPTS:
 * 
 * Spring Data JPA automatically implements this interface at runtime!
 * You don't need to write any implementation code.
 * 
 * JpaRepository<User, Long> provides:
 * - save(entity)       - Insert or update
 * - findById(id)       - Find by primary key
 * - findAll()          - Get all records
 * - delete(entity)     - Remove record
 * - count()            - Count records
 * - existsById(id)     - Check existence
 * 
 * QUERY DERIVATION:
 * Spring Data JPA can derive queries from method names!
 * - findByUsername     → SELECT * FROM users WHERE username = ?
 * - findByEmail        → SELECT * FROM users WHERE email = ?
 * - existsByUsername   → SELECT COUNT(*) > 0 FROM users WHERE username = ?
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     * Used during authentication to load user details
     * 
     * @param username the username to search for
     * @return Optional containing user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     * Useful for "forgot password" functionality
     * 
     * @param email the email to search for
     * @return Optional containing user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists
     * Used during registration to prevent duplicates
     * 
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    Boolean existsByUsername(String username);

    /**
     * Check if email exists
     * Used during registration to prevent duplicates
     * 
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    Boolean existsByEmail(String email);
}
