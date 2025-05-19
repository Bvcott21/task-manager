package com.bucott.taskmanager.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bucott.taskmanager.dto.auth.LoginRequestDTO;
import com.bucott.taskmanager.dto.auth.LoginResponseDTO;
import com.bucott.taskmanager.dto.auth.RegisterRequestDTO;
import com.bucott.taskmanager.dto.auth.RegisterResponseDTO;
import com.bucott.taskmanager.service.UserDetailsServiceImpl;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

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
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        logger.info("Login attempt for user: {}", loginRequestDTO.getUsernameOrEmail());
        
        LoginResponseDTO response = userDetailsService.authenticate(loginRequestDTO);

        return ResponseEntity.ok(response);
    }
    

    // register endpoint
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        logger.info("Register attempt for user: {}", registerRequestDTO.getUsername());
       
        RegisterResponseDTO response = userDetailsService.register(registerRequestDTO);
        return ResponseEntity.ok(response);
    }            

    // refresh token endpoint
    
    // logout endpoint
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        logger.info("Logout attempt");

        userDetailsService.invalidateToken();
        
        return ResponseEntity.ok().build();
    }
    
    
    // forgot password endpoint
    
    // reset password endpoint
    
    // change password endpoint
    
}
