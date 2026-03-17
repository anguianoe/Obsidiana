package com.nexcoyo.knowledge.obsidiana.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashEntityType;
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
@Table(name = "restore_audit", schema = "obsidiana")
public class RestoreAudit extends BaseUuidEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trash_record_id", nullable = false)
    private TrashRecord trashRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 30)
    private TrashEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restored_by", nullable = false)
    private AppUser restoredBy;

    @Column(name = "restore_reason", length = 255)
    private String restoreReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "restore_payload", columnDefinition = "jsonb")
    private JsonNode restorePayload;

    @Column(name = "restored_at", nullable = false)
    private OffsetDateTime restoredAt;
}
