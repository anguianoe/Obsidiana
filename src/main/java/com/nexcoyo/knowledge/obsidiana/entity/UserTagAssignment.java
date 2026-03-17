package com.nexcoyo.knowledge.obsidiana.entity;


import com.nexcoyo.knowledge.obsidiana.util.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_tag_assignment", schema = "obsidiana")
public class UserTagAssignment extends BaseUuidEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_user_id", nullable = false)
    private AppUser targetUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private WorkspaceTag tag;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_status", nullable = false, length = 30)
    private AssignmentStatus assignmentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AppUser createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
