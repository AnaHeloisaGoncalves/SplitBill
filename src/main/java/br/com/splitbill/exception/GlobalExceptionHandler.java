package br.com.splitbill.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import br.com.splitbill.group.exception.GroupNotFoundException;
import br.com.splitbill.group.exception.NotGroupMemberException;
import br.com.splitbill.user.exception.DuplicateEmailException;
import br.com.splitbill.user.exception.UserNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<StandardError> handleBadCredentials(BadCredentialsException e, HttpServletRequest request) {
        log.warn("Authentication failed: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", e.getMessage(), request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<StandardError> handleResponseStatus(ResponseStatusException e, HttpServletRequest request) {
        log.warn("Response status exception: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason(), e.getReason(), request);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<StandardError> handleExpiredJwtException(ExpiredJwtException e, HttpServletRequest request) {
        log.warn("Expired JWT token: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", "Token has expired", request);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<StandardError> handleJwtException(JwtException e, HttpServletRequest request) {
        log.warn("Invalid JWT token: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid JWT token", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardError> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        log.warn("Access denied: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Forbidden", e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("Validation error: {}", e.getMessage());
        String defaultMessage = "Validation error";
        if (e.getBindingResult().getFieldError() != null) {
            defaultMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        }
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", defaultMessage, request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<StandardError> handleUserNotFound(UserNotFoundException e, HttpServletRequest request) {
        log.warn("User not found: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", e.getMessage(), request);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<StandardError> handleDuplicateEmail(DuplicateEmailException e, HttpServletRequest request) {
        log.warn("Duplicate email: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", e.getMessage(), request);
    }

    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<StandardError> handleGroupNotFound(GroupNotFoundException e, HttpServletRequest request) {
        log.warn("Group not found: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", e.getMessage(), request);
    }

    @ExceptionHandler(NotGroupMemberException.class)
    public ResponseEntity<StandardError> handleNotGroupMember(NotGroupMemberException e, HttpServletRequest request) {
        log.warn("Not a group member: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Forbidden", e.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> handleGenericException(Exception e, HttpServletRequest request) {
        log.error("Unexpected error occurred", e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", request);
    }

    private ResponseEntity<StandardError> buildErrorResponse(HttpStatus status, String error, String message, HttpServletRequest request) {
        StandardError err = StandardError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(err);
    }
}
