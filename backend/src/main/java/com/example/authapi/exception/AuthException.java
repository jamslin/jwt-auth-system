package com.example.authapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * =====================================================================
 * AUTHENTICATION EXCEPTION
 * =====================================================================
 * 
 * Custom exception for authentication-related errors.
 * 
 * @ResponseStatus(HttpStatus.UNAUTHORIZED) means this exception
 * will automatically result in a 401 response.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthException extends RuntimeException {
    
    public AuthException(String message) {
        super(message);
    }
    
    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
