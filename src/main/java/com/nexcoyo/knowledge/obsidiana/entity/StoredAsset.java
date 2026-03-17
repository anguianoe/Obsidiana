package com.nexcoyo.knowledge.obsidiana.entity;


import com.nexcoyo.knowledge.obsidiana.util.enums.AssetStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssetType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stored_asset", schema = "obsidiana")
public class StoredAsset extends AuditableTimestampsEntity {

    @Column(name = "storage_provider", nullable = false, length = 30)
    private String storageProvider;

    @Column(name = "bucket_name", nullable = false, length = 255)
    private String bucketName;

    @Column(name = "object_key", nullable = false, columnDefinition = "text")
    private String objectKey;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "normalized_filename", length = 255)
    private String normalizedFilename;

    @Column(name = "mime_type", nullable = false, length = 180)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 30)
    private AssetType assetType;

    @Column(name = "file_extension", length = 30)
    private String fileExtension;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AssetStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private AppUser uploadedBy;
}
