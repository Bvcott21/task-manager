package com.bucott.taskmanager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bucott.taskmanager.dto.auth.LoginRequestDTO;
import com.bucott.taskmanager.dto.auth.LoginResponseDTO;
import com.bucott.taskmanager.dto.auth.RegisterRequestDTO;
import com.bucott.taskmanager.dto.auth.RegisterResponseDTO;
import com.bucott.taskmanager.dto.user.UserInfoDTO;
import com.bucott.taskmanager.exception.AuthException;
import com.bucott.taskmanager.exception.InvalidInputException;
import com.bucott.taskmanager.exception.UsernameOrEmailNotFoundException;
import com.bucott.taskmanager.model.Authority;
import com.bucott.taskmanager.model.Role;
import com.bucott.taskmanager.model.User;
import com.bucott.taskmanager.repository.UserRepository;
import com.bucott.taskmanager.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

        private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
        private static final String AUTH_COOKIE_NAME = "authToken";
        private static final int COOKIE_MAX_AGE = 7 * 24 * 60 * 60;

        private final UserRepository userRepository;
        private final JwtUtil jwtUtil;
        private PasswordEncoder passwordEncoder;

        public UserDetailsServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
                this.jwtUtil = jwtUtil;
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
        }

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                logger.info("Attempting to load user by username: {}", username);
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found with username: " + username));
                logger.info("User found: {}", user);

                return org.springframework.security.core.userdetails.User.builder()
                                .username(user.getUsername())
                                .authorities(user.getRoles().stream()
                                                .map(role -> role.getAuthority().name())
                                                .toArray(String[]::new))
                                .build();
        }

        @Override
        public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
                logger.info("Attempting to load user by email: {}", email);
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found with email: " + email));
                logger.info("User found: {}", user);

                return org.springframework.security.core.userdetails.User.builder()
                                .username(user.getUsername())
                                .authorities(user.getRoles().stream()
                                                .map(role -> role.getAuthority().name())
                                                .toArray(String[]::new))
                                .build();
        }

        @Override
        public UserDetails loadUserByUsernameOrEmail(String identifier) throws UsernameOrEmailNotFoundException {
                User user = userRepository.findByUsernameOrEmail(identifier)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found with username or email: " + identifier));

                return org.springframework.security.core.userdetails.User.builder()
                                .username(user.getUsername())
                                .authorities(user.getRoles().stream()
                                                .map(role -> role.getAuthority().name())
                                                .toArray(String[]::new))
                                .build();
        }

        @Override
        public LoginResponseDTO authenticate(LoginRequestDTO requestDto, HttpServletResponse response) throws UsernameOrEmailNotFoundException {
                User user = userRepository.findByUsernameOrEmail(requestDto.getUsernameOrEmail())
                                .orElseThrow(() -> new UsernameOrEmailNotFoundException(
                                                "User not found with username or email: "
                                                                + requestDto.getUsernameOrEmail()));

                if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
                        throw new AuthException("Invalid password for user: " + requestDto.getUsernameOrEmail());
                }

                String token = jwtUtil.generateToken(user.getUsername(), user.getEmail());

                return LoginResponseDTO.builder()
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .token(token)
                                .build();
        }

        @Override
        public RegisterResponseDTO register(RegisterRequestDTO requestDto, HttpServletResponse response) throws InvalidInputException {
                logger.debug("Registering user with username: {} - email {}", requestDto.getUsername(),
                                requestDto.getEmail());
                if (userRepository.findByUsername(requestDto.getUsername()).isPresent()) {
                        throw new InvalidInputException("Username already exists");
                }
                if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
                        throw new InvalidInputException("Email already exists");
                }

                if (!requestDto.getPassword().equals(requestDto.getConfirmPassword())) {
                        throw new InvalidInputException("Passwords do not match");
                }
                String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

                User user = new User(requestDto.getUsername(), requestDto.getEmail(), encodedPassword);
                user.getRoles().add(new Role(Authority.ROLE_USER));
                user = userRepository.save(user);

                String token = jwtUtil.generateToken(user.getUsername(), user.getEmail());

                setAuthCookie(response, token);

                return RegisterResponseDTO.builder()
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .token(token)
                                .build();
        }

        public void logout(HttpServletResponse response) {
                clearAuthCookie(response);
        }

        public UserInfoDTO getCurrentUser(HttpServletRequest request) {
                String token = extractTokenFromCookie(request.getCookies());
                if (token != null && !token.isEmpty()) {
                        try {
                                String username = jwtUtil.extractUsername(token);
                                String email = jwtUtil.extractEmail(token);

                                if (jwtUtil.validateToken(token, username)) {
                                        return UserInfoDTO.builder()
                                                        .username(username)
                                                        .email(email)
                                                        .authenticated(true)
                                                        .build();
                                }
                        } catch (Exception e) {
                                logger.error("Error extracting user info from token: {}", e.getMessage());
                        }
                }
                return UserInfoDTO.builder()
                                .authenticated(false)
                                .build();
        }

        public void setAuthCookie(HttpServletResponse response, String token) {
                Cookie cookie = new Cookie(AUTH_COOKIE_NAME, token);
                cookie.setHttpOnly(true);
                cookie.setSecure(false);
                cookie.setPath("/");
                cookie.setMaxAge(COOKIE_MAX_AGE);

                response.addCookie(cookie);
                logger.debug("Authentication cookie cleared");
        }

        private void clearAuthCookie(HttpServletResponse response) {
                Cookie cookie = new Cookie(AUTH_COOKIE_NAME, "");
                cookie.setHttpOnly(true);
                cookie.setSecure(false);
                cookie.setPath("/");
                cookie.setMaxAge(0);

                response.addCookie(cookie);
                logger.debug("Authentication cookie cleared");
        }

        public String extractTokenFromCookie(Cookie[] cookies) {
                if (cookies != null) {
                        for (Cookie cookie : cookies) {
                                if (AUTH_COOKIE_NAME.equals(cookie.getName())) {
                                        return cookie.getValue();
                                }
                        }
                }
                return null;
        }

        public boolean validateTokenFromCookie(Cookie[] cookies) {
                String token = extractTokenFromCookie(cookies);
                if (token != null && !token.isEmpty()) {
                        try {
                                String username = jwtUtil.extractUsername(token);
                                return jwtUtil.validateToken(token, username);
                        } catch (Exception e) {
                                logger.error("Error validating token from cookie: {}", e.getMessage());
                                return false;
                        }
                }
                return false;
        }
}