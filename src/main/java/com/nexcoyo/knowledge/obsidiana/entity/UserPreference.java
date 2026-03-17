package com.nexcoyo.knowledge.obsidiana.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_preference", schema = "obsidiana")
public class UserPreference {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "theme", length = 30)
    private String theme;

    @Column(name = "sidebar_collapsed", nullable = false)
    private Boolean sidebarCollapsed;

    @Column(name = "show_private_first", nullable = false)
    private Boolean showPrivateFirst;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
