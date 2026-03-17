package com.nexcoyo.knowledge.obsidiana.service.security;

import com.nexcoyo.knowledge.obsidiana.util.enums.SystemRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@SuppressWarnings({
        "PMD.AvoidDuplicateLiterals"
})
public class JwtService {

    private final String issuer;
    private final Key key;
    private final long accessMinutes;
    private final long refreshDays;

    public JwtService(
            @Value("${app.security.jwt.issuer}") String issuer,
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.accessTokenMinutes}") long accessMinutes,
            @Value("${app.security.refresh.days}") long refreshDays
    ) {
        this.issuer = issuer;
        this.key = Keys.hmacShaKeyFor(secret.getBytes( StandardCharsets.UTF_8));
        this.accessMinutes = accessMinutes;
        this.refreshDays = refreshDays;
    }

    public record TokenPair(String accessToken, String refreshToken, long accessExpiresInSeconds) {}

    public TokenPair issueTokens(String subjectEmail, List<String> roles) {
        var now = Instant.now();

        var accessExp = now.plusSeconds(accessMinutes * 60);
        var refreshExp = now.plusSeconds(refreshDays * 24 * 60 * 60);

        String access = Jwts.builder()
                            .issuer(issuer)
                            .subject(subjectEmail)
                            .claims( Map.of("roles", roles, "typ", "access"))
                            .issuedAt( Date.from(now))
                            .expiration(Date.from(accessExp))
                            .signWith(key)
                            .compact();

        String refresh = Jwts.builder()
                             .issuer(issuer)
                             .subject(subjectEmail)
                             .claims(Map.of("typ", "refresh"))
                             .issuedAt(Date.from(now))
                             .expiration(Date.from(refreshExp))
                             .signWith(key)
                             .compact();

        return new TokenPair(access, refresh, accessExp.getEpochSecond() - now.getEpochSecond());
    }

    public Claims parse( String jwt) {
        return Jwts.parser()
                   .verifyWith((javax.crypto.SecretKey) key)
                   .build()
                   .parseSignedClaims(jwt)
                   .getPayload();
    }

    public boolean isRefreshToken(Claims claims) {
        return "refresh".equals(claims.get("typ", String.class));
    }

    public boolean isAccessToken(Claims claims) {
        return "access".equals(claims.get("typ", String.class));
    }

    public TokenPair issueAccessToken( String email, UUID idUser, SystemRole rol,  List<String> roles) {
        var now = Instant.now();
        var accessExp = now.plusSeconds(accessMinutes * 60);

        String access = Jwts.builder()
                            .issuer(issuer)
                            .subject(email)
                            .claims(Map.of(
                                    "roles", roles,
                                    "userType", rol.name(),
                                    "idUser", idUser,
                                    "typ", "access"
                            ))
                            .issuedAt(Date.from(now))
                            .expiration(Date.from(accessExp))
                            .signWith(key)
                            .compact();

        return new TokenPair(access, null, accessExp.getEpochSecond() - now.getEpochSecond());
    }
}
