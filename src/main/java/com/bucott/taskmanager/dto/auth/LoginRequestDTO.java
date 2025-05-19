package com.bucott.taskmanager.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class LoginRequestDTO {
    private String usernameOrEmail;
    private String password;
}
