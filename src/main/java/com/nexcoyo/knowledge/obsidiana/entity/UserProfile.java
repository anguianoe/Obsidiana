package com.nexcoyo.knowledge.obsidiana.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_profile", schema = "obsidiana")
@ToString
public class UserProfile {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_asset_id")
    private StoredAsset avatarAsset;

    @Column(name = "bio", columnDefinition = "text")
    private String bio;

    @Column(name = "locale", length = 20)
    private String locale;

    @Column(name = "timezone", length = 100)
    private String timezone;

    @Column(name = "city", length = 120)
    private String city;

    @Column(name = "region", length = 120)
    private String region;

    @Column(name = "country", length = 120)
    private String country;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
