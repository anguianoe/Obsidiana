package com.nexcoyo.knowledge.obsidiana.entity;

import com.nexcoyo.knowledge.obsidiana.util.enums.EncryptionScope;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "page_encryption_metadata", schema = "obsidiana")
public class PageEncryptionMetadata {

    @Id
    @Column(name = "page_id", nullable = false)
    private UUID pageId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id")
    private WikiPage page;

    @Column(name = "encryption_algorithm", nullable = false, length = 50)
    private String encryptionAlgorithm;

    @Enumerated(EnumType.STRING)
    @Column(name = "encryption_scope", nullable = false, length = 30)
    private EncryptionScope encryptionScope;

    @Column(name = "is_search_indexed", nullable = false)
    private Boolean isSearchIndexed;

    @Column(name = "public_publishing_blocked", nullable = false)
    private Boolean publicPublishingBlocked;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "updated_by", nullable = false)
    private AppUser updatedBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
