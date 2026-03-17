package com.nexcoyo.knowledge.obsidiana.entity;

import com.nexcoyo.knowledge.obsidiana.util.enums.PublicationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "public_page_publication", schema = "obsidiana")
public class PublicPagePublication extends BaseUuidEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "page_id", nullable = false)
    private WikiPage page;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "revision_id", nullable = false)
    private WikiPageRevision revision;

    @Column(name = "public_slug", nullable = false, unique = true, length = 180)
    private String publicSlug;

    @Column(name = "public_title", nullable = false, length = 255)
    private String publicTitle;

    @Column(name = "public_html", nullable = false, columnDefinition = "text")
    private String publicHtml;

    @Enumerated(EnumType.STRING)
    @Column(name = "publication_status", nullable = false, length = 30)
    private PublicationStatus publicationStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "published_by", nullable = false)
    private AppUser publishedBy;

    @Column(name = "published_at", nullable = false)
    private OffsetDateTime publishedAt;

    @Column(name = "unpublished_at")
    private OffsetDateTime unpublishedAt;
}
