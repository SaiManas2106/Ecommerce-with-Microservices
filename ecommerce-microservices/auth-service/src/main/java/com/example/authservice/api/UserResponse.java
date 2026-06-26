package com.example.authservice.api;

import com.example.authservice.model.AppUser;
import com.example.authservice.model.UserRole;

public record UserResponse(Long id, String email, String fullName, UserRole role) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }
}
