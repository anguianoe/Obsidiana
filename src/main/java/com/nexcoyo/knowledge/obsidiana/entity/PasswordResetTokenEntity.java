package com.nexcoyo.knowledge.obsidiana.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "password_reset_tokens", schema = "obsidiana")
public class PasswordResetTokenEntity {

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

    @Column(name = "used_at")
    private Instant usedAt;

    protected PasswordResetTokenEntity() {}

    public PasswordResetTokenEntity(UUID id, AppUser user, String tokenHash, Instant issuedAt, Instant expiresAt) {
        this.id = id;
        this.user = user;
        this.tokenHash = tokenHash;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public boolean isUsable() {
        return usedAt == null && Instant.now().isBefore(expiresAt);
    }

    public void markUsed(Instant at) {
        this.usedAt = at;
    }
}
