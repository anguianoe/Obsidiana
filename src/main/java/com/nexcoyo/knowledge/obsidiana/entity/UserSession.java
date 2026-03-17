package com.nexcoyo.knowledge.obsidiana.entity;

import com.nexcoyo.knowledge.obsidiana.util.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_session", schema = "obsidiana")
public class UserSession extends BaseUuidEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "refresh_token_hash", nullable = false, columnDefinition = "text")
    private String refreshTokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false, length = 30)
    private SessionStatus sessionStatus;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @Column(name = "device_type", length = 60)
    private String deviceType;

    @Column(name = "os_name", length = 120)
    private String osName;

    @Column(name = "browser_name", length = 120)
    private String browserName;

    @Column(name = "city_name", length = 120)
    private String cityName;

    @Column(name = "region_name", length = 120)
    private String regionName;

    @Column(name = "country_name", length = 120)
    private String countryName;

    @Column(name = "login_at", nullable = false)
    private OffsetDateTime loginAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
