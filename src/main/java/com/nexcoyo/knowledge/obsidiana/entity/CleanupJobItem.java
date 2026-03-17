package com.nexcoyo.knowledge.obsidiana.entity;

import com.nexcoyo.knowledge.obsidiana.util.enums.CleanupJobItemAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "cleanup_job_item", schema = "obsidiana")
public class CleanupJobItem extends BaseUuidEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private CleanupJob job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private StoredAsset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trash_record_id")
    private TrashRecord trashRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private CleanupJobItemAction action;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
