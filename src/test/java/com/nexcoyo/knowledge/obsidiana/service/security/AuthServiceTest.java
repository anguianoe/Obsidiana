package com.nexcoyo.knowledge.obsidiana.service.security;

import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.records.LoginRequest;
import com.nexcoyo.knowledge.obsidiana.repository.AppUserRepository;
import com.nexcoyo.knowledge.obsidiana.repository.PasswordResetTokenJpaRepository;
import com.nexcoyo.knowledge.obsidiana.repository.RefreshTokenJpaRepository;
import com.nexcoyo.knowledge.obsidiana.util.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private RefreshTokenJpaRepository refreshTokens;
    @Mock
    private PasswordResetTokenJpaRepository resetTokens;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(appUserRepository, refreshTokens, resetTokens, encoder, jwtService, 7, 30);
    }

    @Test
    void loginReturnsUnauthorizedWhenPasswordDoesNotMatch() {
        AppUser user = new AppUser();
        user.setEmail("admin@example.com");
        user.setPasswordHash("encoded-password");

        when(appUserRepository.findByEmailAndStatus("admin@example.com", UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(encoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("device-1", "admin@example.com", "wrong-password", false),
                "127.0.0.1",
                "JUnit"
        ))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(ex.getReason()).isEqualTo("Invalid credentials");
                });
    }
}

