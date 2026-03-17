package com.nexcoyo.knowledge.obsidiana.service.specification;

import java.time.OffsetDateTime;

import com.nexcoyo.knowledge.obsidiana.entity.TrashRecord;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.TrashRecordSearchCriteria;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

public final class TrashRecordSpecifications {

    private TrashRecordSpecifications() {
    }

    public static Specification< TrashRecord > byCriteria( TrashRecordSearchCriteria criteria) {
        return Specification.allOf(
            equalsPath("workspace.id", criteria.getWorkspaceId()),
            equalsPath("page.id", criteria.getPageId()),
            equalsPath("asset.id", criteria.getAssetId()),
            equalsPath("deletedBy.id", criteria.getDeletedBy()),
            entityType(criteria.getEntityType()),
            status(criteria.getStatus()),
            overdue(criteria.getOverdue())
        );
    }

    private static Specification<TrashRecord> equalsPath(String path, Object value) {
        return (root, query, cb) -> {
            if (value == null) return null;
            Path<?> current = root;
            for (String part : path.split("\\.")) {
                current = current.get(part);
            }
            return cb.equal(current, value);
        };
    }

    private static Specification<TrashRecord> entityType(Enum<?> entityType) {
        return (root, query, cb) -> entityType == null ? null : cb.equal(root.get("entityType"), entityType);
    }

    private static Specification<TrashRecord> status(Enum<?> status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<TrashRecord> overdue(Boolean overdue) {
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(overdue)) return null;
            return cb.lessThan(root.get("restoreDeadlineAt"), OffsetDateTime.now());
        };
    }
}
