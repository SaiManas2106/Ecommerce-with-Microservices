package com.example.authservice.service;

import com.example.authservice.api.AuthResponse;
import com.example.authservice.api.LoginRequest;
import com.example.authservice.api.RegisterRequest;
import com.example.authservice.api.UserResponse;
import com.example.authservice.exception.AuthException;
import com.example.authservice.model.AppUser;
import com.example.authservice.model.UserRole;
import com.example.authservice.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        AppUser user = new AppUser();
        user.setEmail(request.getEmail().toLowerCase());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.CUSTOMER);
        user = userRepository.save(user);
        return new AuthResponse(tokenService.issue(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException("Invalid credentials");
        }
        return new AuthResponse(tokenService.issue(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse me(String authorizationHeader) {
        String token = authorizationHeader == null ? "" : authorizationHeader.replace("Bearer ", "");
        Long userId = tokenService.validateAndGetUserId(token);
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new AuthException("User no longer exists"));
    }
}
