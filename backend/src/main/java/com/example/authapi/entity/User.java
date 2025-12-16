package com.example.authapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * =====================================================================
 * USER ENTITY
 * =====================================================================
 * 
 * This class represents a user in our system. It implements UserDetails
 * which is required by Spring Security to handle authentication.
 * 
 * KEY CONCEPTS:
 * 
 * @Entity - Marks this class as a JPA entity (maps to database table)
 * @Table  - Specifies the table name and constraints
 * 
 * UserDetails interface provides:
 * - getUsername()      - Returns the username
 * - getPassword()      - Returns the encoded password
 * - getAuthorities()   - Returns the user's roles/permissions
 * - isAccountNonExpired(), isAccountNonLocked(), etc. - Account status checks
 */
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")
})
@Data                    // Lombok: generates getters, setters, toString, equals, hashCode
@NoArgsConstructor       // Lombok: generates no-args constructor (required by JPA)
@AllArgsConstructor      // Lombok: generates all-args constructor
@Builder                 // Lombok: generates builder pattern
public class User implements UserDetails {

    /**
     * Primary key - auto-generated ID
     * GenerationType.IDENTITY uses database auto-increment
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username for login
     * - Cannot be null
     * - Must be unique (enforced at table level)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Email address
     * - Cannot be null
     * - Must be unique
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Encoded password
     * - Never store plain text passwords!
     * - Will be encoded using BCrypt
     */
    @Column(nullable = false)
    private String password;

    /**
     * User roles (ROLE_USER, ROLE_ADMIN, etc.)
     * 
     * @ElementCollection - For simple collection of values
     * FetchType.EAGER - Load roles immediately with user
     *                   (needed for authentication)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    /**
     * Account status flags
     * Used by Spring Security to check if account is usable
     */
    @Builder.Default
    private boolean enabled = true;
    
    @Builder.Default
    private boolean accountNonExpired = true;
    
    @Builder.Default
    private boolean accountNonLocked = true;
    
    @Builder.Default
    private boolean credentialsNonExpired = true;

    /**
     * Audit fields - track when user was created/modified
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle callbacks
     * Automatically set timestamps before insert/update
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // =================================================================
    // UserDetails Interface Implementation
    // =================================================================

    /**
     * Returns the authorities (roles/permissions) granted to the user.
     * Spring Security uses this to check if user has required permissions.
     * 
     * We convert our String roles to GrantedAuthority objects.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * The following methods return account status.
     * If any returns false, authentication will fail.
     */
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
