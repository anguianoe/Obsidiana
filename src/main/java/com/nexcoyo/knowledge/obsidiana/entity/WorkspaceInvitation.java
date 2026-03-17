package com.nexcoyo.knowledge.obsidiana.entity;


import com.nexcoyo.knowledge.obsidiana.util.enums.InvitationStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "workspace_invitation", schema = "obsidiana")
public class WorkspaceInvitation extends AuditableTimestampsEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(name = "invited_email", nullable = false, length = 255)
    private String invitedEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_user_id")
    private AppUser invitedUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private WorkspaceRole role;

    @Column(name = "invitation_token_hash", nullable = false, columnDefinition = "text")
    private String invitationTokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InvitationStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invited_by", nullable = false)
    private AppUser invitedBy;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;
}
