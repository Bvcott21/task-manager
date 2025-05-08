package com.bucott.taskmanager.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bucott.taskmanager.service.UserDetailsServiceImpl;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    // logging dependency
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    UserDetailsServiceImpl userDetailsService;

    public AuthController(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String usernameOrEmail, @RequestParam String password) {
        logger.info("Login attempt for user: {}", usernameOrEmail);
        
        Map<String, Object> response = userDetailsService.authenticate(usernameOrEmail, password);

        return ResponseEntity.ok(response);
    }
    

    // register endpoint
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String username, @RequestParam String email, @RequestParam String password
            , @RequestParam String confirmPassword) {
        logger.info("Register attempt for user: {}", username);
        Map<String, Object> response = userDetailsService.register(username, email, password, confirmPassword);
        return ResponseEntity.ok(response);
    }            

    // refresh token endpoint
    
    // logout endpoint
    
    // forgot password endpoint
    
    // reset password endpoint
    
    // change password endpoint
    
}
