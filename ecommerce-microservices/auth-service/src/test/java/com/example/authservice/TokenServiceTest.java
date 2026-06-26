package com.example.authservice;

import com.example.authservice.exception.AuthException;
import com.example.authservice.model.AppUser;
import com.example.authservice.model.UserRole;
import com.example.authservice.service.TokenService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {

    @Test
    void issueAndValidate_returnsUserId() {
        TokenService tokenService = new TokenService("test-secret", 60);
        AppUser user = user();

        String token = tokenService.issue(user);

        assertThat(tokenService.validateAndGetUserId(token)).isEqualTo(42L);
    }

    @Test
    void validate_rejectsTamperedToken() {
        TokenService tokenService = new TokenService("test-secret", 60);
        String token = tokenService.issue(user()) + "tampered";

        assertThatThrownBy(() -> tokenService.validateAndGetUserId(token))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Invalid token");
    }

    private AppUser user() {
        AppUser user = new AppUser();
        user.setId(42L);
        user.setEmail("customer@example.com");
        user.setFullName("Demo Customer");
        user.setRole(UserRole.CUSTOMER);
        return user;
    }
}
