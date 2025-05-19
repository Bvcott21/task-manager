package com.bucott.taskmanager.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.bucott.taskmanager.dto.auth.LoginRequestDTO;
import com.bucott.taskmanager.dto.auth.LoginResponseDTO;
import com.bucott.taskmanager.dto.auth.RegisterRequestDTO;
import com.bucott.taskmanager.dto.auth.RegisterResponseDTO;
import com.bucott.taskmanager.exception.InvalidInputException;
import com.bucott.taskmanager.exception.UsernameOrEmailNotFoundException;

public interface UserDetailsService extends org.springframework.security.core.userdetails.UserDetailsService {
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException;
    public UserDetails loadUserByUsernameOrEmail(String identifier) throws UsernameNotFoundException;
    public LoginResponseDTO authenticate(LoginRequestDTO requestDto) throws UsernameOrEmailNotFoundException;
    public RegisterResponseDTO register(RegisterRequestDTO requestDto) throws InvalidInputException;
    public void invalidateToken();

}
