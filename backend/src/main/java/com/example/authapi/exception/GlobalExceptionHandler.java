package com.example.authapi.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * =====================================================================
 * GLOBAL EXCEPTION HANDLER
 * =====================================================================
 * 
 * This class handles exceptions thrown by controllers and returns
 * standardized error responses.
 * 
 * @RestControllerAdvice combines @ControllerAdvice and @ResponseBody
 * - @ControllerAdvice: Allows handling exceptions across all controllers
 * - @ResponseBody: Return values are serialized to JSON
 * 
 * Benefits:
 * - Centralized exception handling
 * - Consistent error response format
 * - Clean controller code (no try-catch blocks needed)
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(
            AuthException ex, WebRequest request) {
        
        log.warn("Authentication error: {}", ex.getMessage());
        
        return buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            ex.getMessage(),
            request.getDescription(false)
        );
    }

    /**
     * Handle bad credentials (wrong password, etc.)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {
        
        log.warn("Bad credentials: {}", ex.getMessage());
        
        return buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Invalid username or password",
            request.getDescription(false)
        );
    }

    /**
     * Handle access denied (insufficient permissions)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {
        
        log.warn("Access denied: {}", ex.getMessage());
        
        return buildErrorResponse(
            HttpStatus.FORBIDDEN,
            "Access denied. You don't have permission to access this resource.",
            request.getDescription(false)
        );
    }

    /**
     * Handle validation errors (@Valid annotation failures)
     * 
     * Returns detailed field-level error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        // Collect all field errors
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        log.warn("Validation error: {}", fieldErrors);
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("message", "Invalid request data");
        body.put("errors", fieldErrors);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        body.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handle all other exceptions (catch-all)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected error", ex);
        
        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please try again later.",
            request.getDescription(false)
        );
    }

    /**
     * Build standardized error response
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String message, String path) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path.replace("uri=", ""));
        body.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.status(status).body(body);
    }
}
