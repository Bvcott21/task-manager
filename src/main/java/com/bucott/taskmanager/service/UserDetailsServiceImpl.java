package com.bucott.taskmanager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bucott.taskmanager.dto.auth.LoginRequestDTO;
import com.bucott.taskmanager.dto.auth.LoginResponseDTO;
import com.bucott.taskmanager.dto.auth.RegisterRequestDTO;
import com.bucott.taskmanager.dto.auth.RegisterResponseDTO;
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
    public LoginResponseDTO authenticate(LoginRequestDTO requestDto) throws UsernameOrEmailNotFoundException {
        User user = userRepository.findByUsernameOrEmail(requestDto.getUsernameOrEmail())
                .orElseThrow(() -> new UsernameOrEmailNotFoundException("User not found with username or email: " + requestDto.getUsernameOrEmail()));

        if (!user.getPassword().equals(requestDto.getPassword())) {
            throw new AuthException("Invalid password for user: " + requestDto.getUsernameOrEmail());
        }

        User authenticatedUser = (User) org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> role.getAuthority().name())
                        .toArray(String[]::new))
                .build();
        String token = jwtUtil.generateToken(authenticatedUser.getUsername());
        return LoginResponseDTO.builder()
                .username(authenticatedUser.getUsername())
                .email(authenticatedUser.getEmail())
                .token(token)
                .build();
    }

    @Override
    public RegisterResponseDTO register(RegisterRequestDTO requestDto) throws InvalidInputException {
        logger.debug("Registering user with username: {} - email {}", requestDto.getUsername(), requestDto.getEmail());
        if (userRepository.findByUsername(requestDto.getUsername()).isPresent()) {
                throw new InvalidInputException("Username already exists");
        }
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
                throw new InvalidInputException("Email already exists");
        }

        if (requestDto.getPassword().equals(requestDto.getConfirmPassword())) {
                throw new InvalidInputException("Passwords do not match");
        }

        User user = new User(requestDto.getUsername(), requestDto.getEmail(), requestDto.getPassword());
        user.getRoles().add(new Role(Authority.ROLE_USER));
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());
        return RegisterResponseDTO.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .token(token)
                .build();
}

    public void invalidateToken() {
        // Invalidate the token
        // This can be done by adding the token to a blacklist or by using a short expiration time
        // ands refreshing the token on each request
        // For simplicity, we will just return a success message
        // In a real application, you would also want to invalidate the token on the server side
        // and remove it from the user's session
    }
}