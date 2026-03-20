package com.nexcoyo.knowledge.obsidiana.service.specification;

import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.WorkspaceSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;
import org.springframework.data.jpa.domain.Specification;

public final class WorkspaceSpecifications {

    private WorkspaceSpecifications() {
    }

    public static Specification< Workspace > notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification< Workspace > byCriteria( WorkspaceSearchCriteria criteria) {
        return Specification.allOf(
            notDeleted(),
            likeNameOrSlug(criteria.getNameOrSlug()),
            hasKind(criteria.getKind()),
            hasStatus(criteria.getStatus()),
            hasApprovalStatus(criteria.getApprovalStatus()),
            createdBy(criteria.getCreatedBy()),
            createdBy(criteria.getOnlyOwned() != null && criteria.getOnlyOwned() ? criteria.getUserId() : null),
            memberUser(criteria.getOnlyMember() != null && criteria.getOnlyMember() ? criteria.getUserId() : null)
        );
    }

    public static Specification< Workspace > byCreatedBy(UUID userId, String nameOrSlug, WorkspaceStatus status) {
        return Specification.allOf(
            notDeleted(),
            createdBy(userId),
            likeNameOrSlug(nameOrSlug),
            hasStatus(status)
        );
    }

    public static Specification< Workspace > adminList(WorkspaceStatus status) {
        return Specification.allOf(
            notDeleted(),
            hasStatus(status)
        );
    }

    public static Specification<Workspace> likeNameOrSlug(String value) {
        return (root, query, cb) -> {
            if (value == null || value.isBlank()) return null;
            String pattern = "%" + value.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("slug")), pattern)
            );
        };
    }

    public static Specification<Workspace> hasKind(Enum<?> kind) {
        return (root, query, cb) -> kind == null ? null : cb.equal(root.get("kind"), kind);
    }

    public static Specification<Workspace> hasStatus(Enum<?> status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Workspace> hasApprovalStatus(Enum<?> approvalStatus) {
        return (root, query, cb) -> approvalStatus == null ? null : cb.equal(root.get("approvalStatus"), approvalStatus);
    }

    public static Specification<Workspace> createdBy(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("createdBy").get("id"), userId);
    }

    public static Specification<Workspace> memberUser(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) return null;
            var sq = query.subquery(UUID.class);
            var membership = sq.from( WorkspaceMembership.class);
            sq.select(membership.get("workspace").get("id"));
            sq.where(
                cb.equal(membership.get("workspace").get("id"), root.get("id")),
                cb.equal(membership.get("user").get("id"), userId)
            );
            return cb.exists(sq);
        };
    }

    public static Specification< Workspace > byRelatedUser(UUID userId, String nameOrSlug, WorkspaceStatus status) {
        return Specification.allOf(
            notDeleted(),
            relatedUser(userId),
            likeNameOrSlug(nameOrSlug),
            hasStatus(status)
        );
    }

    public static Specification<Workspace> relatedUser(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) return null;

            var creatorPredicate = cb.equal(root.get("createdBy").get("id"), userId);

            var sq = query.subquery(UUID.class);
            var membership = sq.from(WorkspaceMembership.class);
            sq.select(membership.get("workspace").get("id"));
            sq.where(
                cb.equal(membership.get("workspace").get("id"), root.get("id")),
                cb.equal(membership.get("user").get("id"), userId),
                cb.equal(membership.get("status"), com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus.ACTIVE),
                cb.equal(membership.get("role"), com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole.ADMIN)
            );

            return cb.or(creatorPredicate, cb.exists(sq));
        };
    }
}
