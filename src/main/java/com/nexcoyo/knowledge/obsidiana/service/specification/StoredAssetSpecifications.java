package com.nexcoyo.knowledge.obsidiana.service.specification;

import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.PageAsset;
import com.nexcoyo.knowledge.obsidiana.entity.PageRevisionAssetRef;
import com.nexcoyo.knowledge.obsidiana.entity.PublicPageAsset;
import com.nexcoyo.knowledge.obsidiana.entity.StoredAsset;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.AssetSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public final class StoredAssetSpecifications {

    private StoredAssetSpecifications() {
    }

    public static Specification< StoredAsset > byCriteria( AssetSearchCriteria criteria) {
        return Specification.allOf(
            uploadedBy(criteria.getUploadedBy()),
            assetType(criteria.getAssetType()),
            status(criteria.getStatus()),
            fileNameOrKey(criteria.getFileNameOrObjectKey()),
            mimeTypePrefix(criteria.getMimeTypePrefix()),
            orphanOnly(criteria.getOnlyOrphans())
        );
    }

    public static Specification<StoredAsset> uploadedBy(UUID uploadedBy) {
        return (root, query, cb) -> uploadedBy == null ? null : cb.equal(root.get("uploadedBy").get("id"), uploadedBy);
    }

    public static Specification<StoredAsset> assetType(Enum<?> assetType) {
        return (root, query, cb) -> assetType == null ? null : cb.equal(root.get("assetType"), assetType);
    }

    public static Specification<StoredAsset> status(Enum<?> status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<StoredAsset> fileNameOrKey(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) return null;
            String pattern = "%" + text.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("originalFilename")), pattern),
                cb.like(cb.lower(root.get("normalizedFilename")), pattern),
                cb.like(cb.lower(root.get("objectKey")), pattern)
            );
        };
    }

    public static Specification<StoredAsset> mimeTypePrefix(String prefix) {
        return (root, query, cb) -> prefix == null || prefix.isBlank() ? null : cb.like(cb.lower(root.get("mimeType")), prefix.toLowerCase() + "%");
    }

    public static Specification<StoredAsset> orphanOnly(Boolean orphanOnly) {
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(orphanOnly)) return null;
            var pageSq = query.subquery(UUID.class);
            var pageAsset = pageSq.from( PageAsset.class);
            pageSq.select(pageAsset.get("asset").get("id"));
            pageSq.where(cb.equal(pageAsset.get("asset").get("id"), root.get("id")));

            var revisionSq = query.subquery(UUID.class);
            var revisionAsset = revisionSq.from( PageRevisionAssetRef.class);
            revisionSq.select(revisionAsset.get("asset").get("id"));
            revisionSq.where(cb.equal(revisionAsset.get("asset").get("id"), root.get("id")));

            var publicSq = query.subquery(UUID.class);
            var publicAsset = publicSq.from( PublicPageAsset.class);
            publicSq.select(publicAsset.get("asset").get("id"));
            publicSq.where(cb.equal(publicAsset.get("asset").get("id"), root.get("id")));

            return cb.and(cb.not(cb.exists(pageSq)), cb.not(cb.exists(revisionSq)), cb.not(cb.exists(publicSq)));
        };
    }
}
