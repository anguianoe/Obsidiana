package com.nexcoyo.knowledge.obsidiana.service.security;

import com.nexcoyo.knowledge.obsidiana.config.TokenHashingConfig;
import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.PasswordResetTokenEntity;
import com.nexcoyo.knowledge.obsidiana.entity.RefreshTokenEntity;
import com.nexcoyo.knowledge.obsidiana.records.ForgotPasswordRequest;
import com.nexcoyo.knowledge.obsidiana.records.LoginRequest;
import com.nexcoyo.knowledge.obsidiana.records.RefreshRequest;
import com.nexcoyo.knowledge.obsidiana.records.ResetPasswordRequest;
import com.nexcoyo.knowledge.obsidiana.repository.AppUserRepository;
import com.nexcoyo.knowledge.obsidiana.repository.PasswordResetTokenJpaRepository;
import com.nexcoyo.knowledge.obsidiana.repository.RefreshTokenJpaRepository;
import com.nexcoyo.knowledge.obsidiana.util.enums.SystemRole;
import com.nexcoyo.knowledge.obsidiana.util.enums.UserStatus;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final RefreshTokenJpaRepository refreshTokens;
    private final PasswordResetTokenJpaRepository resetTokens;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    private final long refreshDays;
    private final long resetTokenMinutes;

    @Value("${app.security.max-sessions:2}")
    private int MAX_SESSIONS;

    @Value("${spring.base_reset_url:http://localhost/reset?token=}")
    private String BASE_PATH_RESET_PASSWORD;

    @Value("${spring.email-contact:no-reply@obsidiana.local}")
    private String EMAIL_CONTACT;

    @Value("${spring.application.name}")
    private String APP_NAME;

    public AuthService( AppUserRepository userRepo, RefreshTokenJpaRepository refreshTokens, PasswordResetTokenJpaRepository resetTokens, PasswordEncoder encoder, JwtService jwtService, @Value( "${app.security.refresh.days}" ) long refreshDays, @Value( "${app.security.resetPassword.tokenMinutes}" ) long resetTokenMinutes )
    {
        this.appUserRepository = userRepo;
        this.refreshTokens = refreshTokens;
        this.resetTokens = resetTokens;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.refreshDays = refreshDays;
        this.resetTokenMinutes = resetTokenMinutes;
    }

    @Transactional
    public ResponseEntity< Map<String, Object> > login( LoginRequest req, String ip, String userAgent) {

        AppUser user = this.appUserRepository.findByEmailAndStatus( req.email(), UserStatus.ACTIVE )
                                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        List< RefreshTokenEntity > active = refreshTokens.findActiveByUserOldestFirst(user.getId(), Instant.now());

        int maxSessions = MAX_SESSIONS;
        int allowedBeforeNew = maxSessions - 1;

        if (active.size() > allowedBeforeNew) {
            int toRevoke = active.size() - allowedBeforeNew;
            Instant now = Instant.now();
            for (int i = 0; i < toRevoke; i++) {
                var victim = active.get(i);
                victim.revoke(now, null);
                refreshTokens.save(victim);
            }
        }

        // Access JWT
        List<String> roles = List.of(user.getRoles().split(","));
        JwtService.TokenPair access = jwtService.issueAccessToken(user.getEmail(), user.getId(),user.getSystemRole().name(), roles);


        // Refresh token (opaque) stored hashed
        String refreshRaw = TokenHashingConfig.newOpaqueToken();
        String refreshHash = TokenHashingConfig.sha256(refreshRaw);

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshDays * 24 * 60 * 60);

        RefreshTokenEntity rt = new RefreshTokenEntity( UUID.randomUUID(), user, refreshHash, now, exp, ip, userAgent, req.deviceId());
        refreshTokens.save(rt);

        user.setLastLoginAt( Instant.now() );
        appUserRepository.save(user);

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("accessToken", access.accessToken());
        body.put("refreshToken", refreshRaw);
        body.put("tokenType", "Bearer");
        body.put("roles", roles);
        body.put("expiresInSeconds", access.accessExpiresInSeconds());
        if(log.isInfoEnabled()){
            log.info("Session started for user={}", user.getEmail());
        }
        return ResponseEntity.ok(body);
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> refresh( RefreshRequest refreshRequest, String ip, String userAgent) {

        String hash = TokenHashingConfig.sha256(refreshRequest.refreshToken());

        RefreshTokenEntity currentTokenHash = refreshTokens.findByTokenHash(hash)
                                                           .orElseThrow(() -> new ResponseStatusException( HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (!currentTokenHash.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        AppUser user = appUserRepository.findByEmailAndStatus( currentTokenHash.getUser().getEmail() , UserStatus.ACTIVE)
                                  .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user for this token"));

        // Rotate refresh
        String newRefreshRaw = TokenHashingConfig.newOpaqueToken();
        String newRefreshHash = TokenHashingConfig.sha256(newRefreshRaw);

        Instant now = Instant.now();
        Instant newExp = now.plusSeconds(refreshDays * 24 * 60 * 60);

        String reqDeviceId = refreshRequest.deviceId().trim();
        if ( reqDeviceId.isBlank() ) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing device ID");
        }

        if (currentTokenHash.getDeviceId() != null && !currentTokenHash.getDeviceId().equals(reqDeviceId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid device token");
        }

        boolean uaChanged = currentTokenHash.getUserAgent() != null && userAgent != null
                && !currentTokenHash.getUserAgent().equals(userAgent);

        boolean ipChanged = currentTokenHash.getIp() != null && ip != null
                && !currentTokenHash.getIp().equals(ip);

        if (uaChanged || ipChanged) {
            if(log.isInfoEnabled()){
                log.info("Risk signal detected for user={} deviceId={}: uaChanged={} ipChanged={}",
                        user.getEmail(), reqDeviceId, uaChanged, ipChanged);
            }
        }

        RefreshTokenEntity next = new RefreshTokenEntity(
                UUID.randomUUID(),
                user,
                newRefreshHash,
                now,
                newExp,
                ip,
                userAgent,
                reqDeviceId
        );
        refreshTokens.save(next);

        currentTokenHash.revoke(now, next.getId());
        refreshTokens.save(currentTokenHash);

        List<String> roles = List.of(user.getRoles().split(","));
        JwtService.TokenPair access = jwtService.issueAccessToken(user.getEmail(),user.getId(), user.getSystemRole().name(), roles);

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("accessToken", access.accessToken());
        body.put("refreshToken", newRefreshRaw);
        body.put("tokenType", "Bearer");
        body.put("roles", roles);
        body.put("expiresInSeconds", access.accessExpiresInSeconds());
        return ResponseEntity.ok(body);
    }

    @Transactional
    public void logout( RefreshRequest req) {
        String hash = TokenHashingConfig.sha256(req.refreshToken());
        refreshTokens.findByTokenHash(hash).ifPresent(rt -> {
            if (rt.getRevokedAt() == null) {
                rt.revoke(Instant.now(), null);
                refreshTokens.save(rt);
            }
        });
    }

    @Transactional
    public ResponseEntity<Map<String, Object>>  forgotPassword( ForgotPasswordRequest req) {

        appUserRepository.findByEmailAndStatus(req.email(),UserStatus.ACTIVE).ifPresent( user -> {

            String raw = TokenHashingConfig.newOpaqueToken();
            String hash = TokenHashingConfig.sha256(raw);

            Instant now = Instant.now();
            Instant exp = now.plusSeconds(resetTokenMinutes * 60);
            LocalDateTime expired = exp.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();

            resetTokens.save(new PasswordResetTokenEntity(UUID.randomUUID(), user, hash, now, exp));

            log.info( "Se mandó el email con token: {}{}", BASE_PATH_RESET_PASSWORD, raw );
/*
            String html = ResetPasswordTemplate.buildHtml(
                    APP_NAME,
                    user.getName() + " " + user.getLastname(),
                    BASE_PATH_RESET_PASSWORD+raw,
                    expired
            );

            emailService.sendHtmlEmail(
                    user.getEmail(),
                    "Restablece tu contraseña",
                    html,
                    EMAIL_CONTACT,
                    APP_NAME
            );
*/
        });
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("message", "If the email exists, a reset link was sent");
        if(log.isInfoEnabled()){
            log.info("Request password reset successfully for user={}", req.email());
        }
        return ResponseEntity.ok(body);
    }

    @Transactional
    public ResponseEntity<Map<String, Object>>  resetPassword( ResetPasswordRequest req) {

        String hash = TokenHashingConfig.sha256(req.token());

        PasswordResetTokenEntity prt = resetTokens.findByTokenHash(hash)
                                                  .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        if (!prt.isUsable()) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        AppUser user = prt.getUser();
        if(log.isInfoEnabled()){
            log.info("Resetting password for user={}", user.getEmail());
        }
        user.setPasswordHash(encoder.encode(req.newPassword()));
        appUserRepository.save(user);

        prt.markUsed(Instant.now());
        resetTokens.save(prt);

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("message", "Password has been reset successfully");
        if(log.isInfoEnabled()){
            log.info("Reset password successfully for user={}", user.getEmail());
        }
        return ResponseEntity.ok(body);
    }
}
