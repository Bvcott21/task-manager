package com.bucott.taskmanager.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bucott.taskmanager.exception.AuthException;
import com.bucott.taskmanager.exception.InvalidInputException;
import com.bucott.taskmanager.exception.UsernameOrEmailNotFoundException;
import com.bucott.taskmanager.model.Authority;
import com.bucott.taskmanager.model.Role;
import com.bucott.taskmanager.model.User;
import com.bucott.taskmanager.repository.UserRepository;
import com.bucott.taskmanager.util.JwtUtil;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserDetailsServiceImpl(UserRepository userRepository, JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        logger.info("User found: {}", user);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> role.getAuthority().name())
                        .toArray(String[]::new))
                .build();
    }

    @Override
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        logger.info("Attempting to load user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        logger.info("User found: {}", user);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> role.getAuthority().name())
                        .toArray(String[]::new))
                .build();
    }

    @Override
    public UserDetails loadUserByUsernameOrEmail(String identifier) throws UsernameOrEmailNotFoundException {
        User user = userRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + identifier));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> role.getAuthority().name())
                        .toArray(String[]::new))
                .build();
    }

    @Override
    public Map<String, Object> authenticate(String identifier, String password) throws UsernameOrEmailNotFoundException {
        User user = userRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new UsernameOrEmailNotFoundException("User not found with username or email: " + identifier));

        if (!user.getPassword().equals(password)) {
            throw new AuthException("Invalid password for user: " + identifier);
        }

        User authenticatedUser = (User) org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> role.getAuthority().name())
                        .toArray(String[]::new))
                .build();
        String token = jwtUtil.generateToken(authenticatedUser.getUsername());
        return Map.of(
                "user", authenticatedUser,
                "token", token
        );
    }

    @Override
    public Map<String, Object> register(String username, String email, String password, String confirmPassword) throws InvalidInputException {
        if (!password.equals(confirmPassword)) {
            throw new InvalidInputException("Passwords do not match");
        }

        User user = new User(username, email, password);
        user.getRoles().add(new Role(Authority.ROLE_USER));
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());
        return Map.of(
                "user", user,
                "token", token
        );
    }
}