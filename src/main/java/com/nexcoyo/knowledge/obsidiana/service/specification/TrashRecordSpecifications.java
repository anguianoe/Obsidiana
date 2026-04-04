package com.nexcoyo.knowledge.obsidiana.service.specification;

import java.time.Instant;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.PageComment;
import com.nexcoyo.knowledge.obsidiana.entity.PageWorkspaceLink;
import com.nexcoyo.knowledge.obsidiana.entity.StoredAsset;
import com.nexcoyo.knowledge.obsidiana.entity.TrashRecord;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.TrashRecordSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public final class TrashRecordSpecifications {

    private static final String WORKSPACE = "workspace";
    private static final String STATUS = "status";

    private TrashRecordSpecifications() {
    }

    public static Specification< TrashRecord > byCriteria( TrashRecordSearchCriteria criteria) {
        return Specification.allOf(
            equalsPath(WORKSPACE + ".id", criteria.getWorkspaceId()),
            equalsPath("page.id", criteria.getPageId()),
            equalsPath("asset.id", criteria.getAssetId()),
            equalsPath("deletedBy.id", criteria.getDeletedBy()),
            entityType(criteria.getEntityType()),
            status(criteria.getStatus()),
            overdue(criteria.getOverdue())
        );
    }

    public static Specification<TrashRecord> visibleToUser(TrashRecordSearchCriteria criteria, UUID userId) {
        return Specification.allOf(
            equalsPath(WORKSPACE + ".id", criteria.getWorkspaceId()),
            equalsPath("page.id", criteria.getPageId()),
            equalsPath("asset.id", criteria.getAssetId()),
            entityType(criteria.getEntityType()),
            status(criteria.getStatus()),
            overdue(criteria.getOverdue()),
            accessibleToUser(userId)
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
        return (root, query, cb) -> status == null ? null : cb.equal(root.get(STATUS), status);
    }

    private static Specification<TrashRecord> overdue(Boolean overdue) {
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(overdue)) return null;
            return cb.lessThan(root.get("restoreDeadlineAt"), Instant.now());
        };
    }

    private static Specification<TrashRecord> accessibleToUser(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return null;
            }

            query.distinct(true);

            Join<TrashRecord, AppUser> deletedBy = root.join("deletedBy", JoinType.LEFT);
            Join<TrashRecord, Workspace> workspace = root.join(WORKSPACE, JoinType.LEFT);
            Join<TrashRecord, WikiPage> page = root.join("page", JoinType.LEFT);
            Join<TrashRecord, PageComment> comment = root.join("comment", JoinType.LEFT);
            Join<TrashRecord, StoredAsset> asset = root.join("asset", JoinType.LEFT);

            Predicate deletedByUser = cb.equal(deletedBy.get("id"), userId);
            Predicate workspaceCreator = cb.equal(workspace.join("createdBy", JoinType.LEFT).get("id"), userId);
            Predicate workspaceMember = existsActiveMembership(query, cb, workspace.get("id"), userId);
            Predicate pageOwner = cb.equal(page.join("ownerUser", JoinType.LEFT).get("id"), userId);
            Predicate pageMember = existsActivePageMembership(query, cb, page.get("id"), userId);
            Predicate commentAuthor = cb.equal(comment.join("authorUser", JoinType.LEFT).get("id"), userId);
            Predicate commentWorkspaceMember = existsActiveMembership(
                query,
                cb,
                comment.join(WORKSPACE, JoinType.LEFT).get("id"),
                userId
            );
            Predicate assetUploader = cb.equal(asset.join("uploadedBy", JoinType.LEFT).get("id"), userId);

            return cb.or(
                deletedByUser,
                workspaceCreator,
                workspaceMember,
                pageOwner,
                pageMember,
                commentAuthor,
                commentWorkspaceMember,
                assetUploader
            );
        };
    }

    private static Predicate existsActiveMembership(
        jakarta.persistence.criteria.CriteriaQuery<?> query,
        jakarta.persistence.criteria.CriteriaBuilder cb,
        jakarta.persistence.criteria.Expression<UUID> workspaceIdExpression,
        UUID userId
    ) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        jakarta.persistence.criteria.Root<WorkspaceMembership> membership = subquery.from(WorkspaceMembership.class);
        subquery.select(cb.literal(1));
        subquery.where(
            cb.equal(membership.get("workspace").get("id"), workspaceIdExpression),
            cb.equal(membership.get("user").get("id"), userId),
            cb.equal(membership.get(STATUS), MembershipStatus.ACTIVE)
        );
        return cb.exists(subquery);
    }

    private static Predicate existsActivePageMembership(
        jakarta.persistence.criteria.CriteriaQuery<?> query,
        jakarta.persistence.criteria.CriteriaBuilder cb,
        jakarta.persistence.criteria.Expression<UUID> pageIdExpression,
        UUID userId
    ) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        jakarta.persistence.criteria.Root<PageWorkspaceLink> pageWorkspaceLink = subquery.from(PageWorkspaceLink.class);
        jakarta.persistence.criteria.Root<WorkspaceMembership> membership = subquery.from(WorkspaceMembership.class);
        subquery.select(cb.literal(1));
        subquery.where(
            cb.equal(pageWorkspaceLink.get("page").get("id"), pageIdExpression),
            cb.equal(membership.get("workspace").get("id"), pageWorkspaceLink.get("workspace").get("id")),
            cb.equal(membership.get("user").get("id"), userId),
            cb.equal(membership.get(STATUS), MembershipStatus.ACTIVE)
        );
        return cb.exists(subquery);
    }
}
