package com.bucott.taskmanager.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.bucott.taskmanager.exception.AuthException;
import com.bucott.taskmanager.exception.UsernameOrEmailNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler({UsernameNotFoundException.class, UsernameOrEmailNotFoundException.class})
    public ResponseEntity<Object> handleUserNotFoundExceptions(Exception ex, WebRequest request) {
        return buildErrorResponse(ex, "User not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Object> handleAuthException(AuthException ex, WebRequest request) {
        return buildErrorResponse(ex, "Authentication Error", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        return buildErrorResponse(ex, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    private ResponseEntity<Object> buildErrorResponse(Exception ex, String error, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", error);
        body.put("message", ex.getMessage());
        body.put("status", status.value());

        return new ResponseEntity<>(body, status);
    }
}