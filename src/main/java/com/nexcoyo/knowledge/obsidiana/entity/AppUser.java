package com.nexcoyo.knowledge.obsidiana.entity;


import com.nexcoyo.knowledge.obsidiana.util.enums.SystemRole;
import com.nexcoyo.knowledge.obsidiana.util.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "app_user", schema = "obsidiana")
public class AppUser extends AuditableTimestampsEntity {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "username", nullable = false, unique = true, length = 120)
    private String username;

    @Column(name = "password_hash", nullable = false, columnDefinition = "text")
    private String passwordHash;

    @Enumerated( EnumType.STRING)
    @Column(name = "system_role", nullable = false, length = 30)
    private SystemRole systemRole;

    @Column(nullable = false)
    private String roles;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserStatus status;

    @Column(name = "first_login_at")
    private Instant firstLoginAt;

    @Column(name = "has_completed_onboarding", nullable = false)
    private Boolean hasCompletedOnboarding;

    @Column(name = "onboarding_version", length = 20)
    private String onboardingVersion;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;
}
