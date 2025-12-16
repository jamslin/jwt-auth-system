package com.example.authapi.security;

import com.example.authapi.entity.User;
import com.example.authapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * =====================================================================
 * CUSTOM USER DETAILS SERVICE
 * =====================================================================
 * 
 * This service is used by Spring Security to load user information
 * during authentication.
 * 
 * WHEN IS THIS CALLED?
 * 
 * 1. During login: to verify username exists and get password hash
 * 2. During JWT validation: to reload user details from database
 * 
 * WHY DO WE NEED THIS?
 * 
 * Spring Security doesn't know how we store users (database schema,
 * table names, etc.). This service bridges our User entity with
 * Spring Security's UserDetails interface.
 * 
 * FLOW:
 * 
 * Login Request → AuthenticationManager → UserDetailsService
 *                                                ↓
 *                                         loadUserByUsername()
 *                                                ↓
 *                                         UserRepository.findByUsername()
 *                                                ↓
 *                                         Return UserDetails (our User entity)
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username
     * 
     * This method is called by Spring Security during authentication.
     * 
     * @param username The username to look up
     * @return UserDetails object (our User entity implements this)
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)  // Read-only transaction for performance
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // Look up user in database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found with username: " + username
                ));
        
        // Our User entity already implements UserDetails,
        // so we can return it directly
        return user;
        
        /*
         * ALTERNATIVE: If your User entity doesn't implement UserDetails,
         * you would build a UserDetails object here:
         * 
         * return org.springframework.security.core.userdetails.User.builder()
         *     .username(user.getUsername())
         *     .password(user.getPassword())
         *     .authorities(user.getRoles().toArray(new String[0]))
         *     .accountExpired(!user.isAccountNonExpired())
         *     .accountLocked(!user.isAccountNonLocked())
         *     .credentialsExpired(!user.isCredentialsNonExpired())
         *     .disabled(!user.isEnabled())
         *     .build();
         */
    }
}
