package com.nexcoyo.knowledge.obsidiana.entity;

import com.nexcoyo.knowledge.obsidiana.util.enums.CleanupJobStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.CleanupJobType;
import com.nexcoyo.knowledge.obsidiana.util.enums.TriggeredBy;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "cleanup_job", schema = "obsidiana")
public class CleanupJob extends BaseUuidEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 50)
    private CleanupJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CleanupJobStatus status;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "total_scanned", nullable = false)
    private Integer totalScanned;

    @Column(name = "total_marked", nullable = false)
    private Integer totalMarked;

    @Column(name = "total_deleted", nullable = false)
    private Integer totalDeleted;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "triggered_by", nullable = false, length = 30)
    private TriggeredBy triggeredBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
