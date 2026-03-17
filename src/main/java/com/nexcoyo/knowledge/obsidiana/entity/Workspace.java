package com.nexcoyo.knowledge.obsidiana.entity;

import com.nexcoyo.knowledge.obsidiana.util.enums.ApprovalStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceKind;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "workspace", schema = "obsidiana")
public class Workspace extends AuditableTimestampsEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 160)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 30)
    private WorkspaceKind kind;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private WorkspaceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 30)
    private ApprovalStatus approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private AppUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private AppUser approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "description", columnDefinition = "text")
    private String description;
}
