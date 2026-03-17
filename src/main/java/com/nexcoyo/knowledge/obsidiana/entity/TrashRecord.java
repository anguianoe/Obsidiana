package com.nexcoyo.knowledge.obsidiana.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashEntityType;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "trash_record", schema = "obsidiana")
public class TrashRecord extends BaseUuidEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 30)
    private TrashEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id")
    private WikiPage page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private StoredAsset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private PageComment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private AppUser deletedBy;

    @Column(name = "delete_reason", length = 255)
    private String deleteReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_payload", columnDefinition = "jsonb")
    private JsonNode snapshotPayload;

    @Column(name = "deleted_at", nullable = false)
    private OffsetDateTime deletedAt;

    @Column(name = "restore_deadline_at")
    private OffsetDateTime restoreDeadlineAt;

    @Column(name = "restored_at")
    private OffsetDateTime restoredAt;

    @Column(name = "purge_scheduled_at")
    private OffsetDateTime purgeScheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TrashStatus status;
}
