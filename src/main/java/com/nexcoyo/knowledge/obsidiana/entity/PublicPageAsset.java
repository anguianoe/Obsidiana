package com.nexcoyo.knowledge.obsidiana.entity;

import com.nexcoyo.knowledge.obsidiana.util.enums.PublicPageAssetRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "public_page_asset", schema = "obsidiana")
public class PublicPageAsset extends BaseUuidEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publication_id", nullable = false)
    private PublicPagePublication publication;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private StoredAsset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_role", nullable = false, length = 30)
    private PublicPageAssetRole assetRole;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
