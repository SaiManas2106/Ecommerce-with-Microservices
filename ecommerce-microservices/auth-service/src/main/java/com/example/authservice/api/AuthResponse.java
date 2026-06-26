package com.example.authservice.api;

public record AuthResponse(String token, UserResponse user) {
}
