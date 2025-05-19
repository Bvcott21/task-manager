package com.bucott.taskmanager.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class RegisterResponseDTO {
    private String username;
    private String email;
    private String token;
}
