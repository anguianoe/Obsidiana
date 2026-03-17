package com.nexcoyo.knowledge.obsidiana.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens", schema = "obsidiana")
public class RefreshTokenEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 128)
    private String tokenHash;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "rotated_to")
    private UUID rotatedTo;

    @Column(name = "ip", length = 64)
    private String ip;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "device_id", length = 64)
    private String deviceId;

    protected RefreshTokenEntity() {}

    public RefreshTokenEntity(
            UUID id,
            AppUser user,
            String tokenHash,
            Instant issuedAt,
            Instant expiresAt,
            String ip,
            String userAgent,
            String deviceId
    ) {
        this.id = id;
        this.user = user;
        this.tokenHash = tokenHash;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.ip = ip;
        this.userAgent = userAgent;
        this.deviceId = deviceId;
    }

    public boolean isActive() {
        return revokedAt == null && Instant.now().isBefore(expiresAt);
    }

    public void revoke(Instant at, UUID rotatedTo) {
        this.revokedAt = at;
        this.rotatedTo = rotatedTo;
    }
}

