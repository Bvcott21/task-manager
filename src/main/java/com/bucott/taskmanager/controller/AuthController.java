package com.bucott.taskmanager.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bucott.taskmanager.dto.auth.LoginRequestDTO;
import com.bucott.taskmanager.dto.auth.LoginResponseDTO;
import com.bucott.taskmanager.dto.auth.RegisterRequestDTO;
import com.bucott.taskmanager.dto.auth.RegisterResponseDTO;
import com.bucott.taskmanager.service.UserDetailsServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
    name = "Authentication", 
    description = "Authentication and authorization endpoints"
)
public class AuthController {
    // logging dependency
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    UserDetailsServiceImpl userDetailsService;

    public AuthController(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    
    @Operation(
        summary = "Login",
        description = "Authenticate a user and return a JWT token",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Successful login",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginResponseDTO.class)
                )
            ),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Bad request")
        }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        logger.info("Login attempt for user: {}", loginRequestDTO.getUsernameOrEmail());
        
        LoginResponseDTO response = userDetailsService.authenticate(loginRequestDTO);

        return ResponseEntity.ok(response);
    }
    

    // register endpoint
    @Operation(
        summary = "Register",
        description = "Register a new user",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Successful registration",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegisterResponseDTO.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Bad request")
        }
    )
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
