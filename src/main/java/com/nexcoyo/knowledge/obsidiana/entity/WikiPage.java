package com.nexcoyo.knowledge.obsidiana.entity;

import com.nexcoyo.knowledge.obsidiana.util.enums.EditMode;
import com.nexcoyo.knowledge.obsidiana.util.enums.PageStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "wiki_page", schema = "obsidiana")
public class WikiPage extends AuditableTimestampsEntity {

    @Column(name = "public_uuid", nullable = false, unique = true)
    private UUID publicUuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private AppUser ownerUser;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "slug", nullable = false, length = 180)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "edit_mode", nullable = false, length = 30)
    private EditMode editMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "page_status", nullable = false, length = 30)
    private PageStatus pageStatus;

    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted;

    @Column(name = "is_publicable", nullable = false)
    private Boolean isPublicable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_revision_id")
    private WikiPageRevision currentRevision;
}
