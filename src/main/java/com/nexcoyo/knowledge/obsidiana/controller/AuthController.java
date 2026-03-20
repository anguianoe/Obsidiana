package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.records.ForgotPasswordRequest;
import com.nexcoyo.knowledge.obsidiana.records.LoginRequest;
import com.nexcoyo.knowledge.obsidiana.records.RefreshRequest;
import com.nexcoyo.knowledge.obsidiana.records.ResetPasswordRequest;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import com.nexcoyo.knowledge.obsidiana.service.security.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public ResponseEntity< Map<String, Object> > login( @Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        return authService.login(req, GeneralService.clientIp(http), GeneralService.userAgent(http));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>>  refresh( @Valid @RequestBody RefreshRequest req, HttpServletRequest http) {
        return authService.refresh(req, GeneralService.clientIp(http), GeneralService.userAgent(http));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout( @Valid @RequestBody RefreshRequest req) {
        authService.logout(req);
        return ResponseEntity.ok(Map.of("logOut", "ok"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>>  forgotPassword( @Valid @RequestBody ForgotPasswordRequest req) {
        return authService.forgotPassword(req);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>>  resetPassword( @Valid @RequestBody ResetPasswordRequest req) {
        return authService.resetPassword(req);
    }
}
