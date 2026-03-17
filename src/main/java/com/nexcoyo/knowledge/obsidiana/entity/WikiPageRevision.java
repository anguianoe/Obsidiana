package com.nexcoyo.knowledge.obsidiana.entity;

import com.nexcoyo.knowledge.obsidiana.util.enums.EditorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "wiki_page_revision", schema = "obsidiana")
public class WikiPageRevision extends BaseUuidEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "page_id", nullable = false)
    private WikiPage page;

    @Column(name = "revision_number", nullable = false)
    private Integer revisionNumber;

    @Column(name = "title_snapshot", nullable = false, length = 255)
    private String titleSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "editor_type", nullable = false, length = 30)
    private EditorType editorType;

    @Column(name = "content_html", columnDefinition = "text")
    private String contentHtml;

    @Column(name = "content_text", columnDefinition = "text")
    private String contentText;

    @Column(name = "change_summary", length = 500)
    private String changeSummary;

    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted;

    // Store ciphertext as PostgreSQL BYTEA (not OID/large object).
    @Column(name = "content_ciphertext", columnDefinition = "bytea")
    private byte[] contentCiphertext;

    @Column(name = "content_iv", length = 255)
    private String contentIv;

    @Column(name = "content_auth_tag", length = 255)
    private String contentAuthTag;

    @Column(name = "encryption_kdf", length = 255)
    private String encryptionKdf;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private AppUser createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
