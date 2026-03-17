package com.nexcoyo.knowledge.obsidiana.service.specification;

import com.nexcoyo.knowledge.obsidiana.dto.request.UserSearchRequest;
import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.UserTagAssignment;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssignmentStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.SystemRole;
import com.nexcoyo.knowledge.obsidiana.util.enums.UserStatus;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class AppUserSpecifications {

    private AppUserSpecifications() {
    }

    public static Specification<AppUser> from(UserSearchRequest request) {
        if (request == null) {
            return Specification.where(notDeletedByDefault(null));
        }
        return Specification.where(textContains(request.text()))
                .and(emailEquals(request.email()))
                .and(usernameEquals(request.username()))
                .and(hasSystemRole(request.systemRole()))
                .and(hasStatus(request.status()))
                .and(hasWorkspaceId(request.workspaceId()))
                .and(hasTagId(request.tagId()))
                .and(createdAtGreaterThanOrEqual(request.createdFrom()))
                .and(createdAtLessThanOrEqual(request.createdTo()))
                .and(notDeletedByDefault(request.includeDeleted()));
    }

    public static Specification<AppUser> textContains(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) {
                return null;
            }
            query.distinct(true);
            String like = "%" + text.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("username")), like)
            );
        };
    }

    public static Specification<AppUser> emailEquals(String email) {
        return (root, query, cb) -> email == null || email.isBlank()
                ? null
                : cb.equal(cb.lower(root.get("email")), email.trim().toLowerCase());
    }

    public static Specification<AppUser> usernameEquals(String username) {
        return (root, query, cb) -> username == null || username.isBlank()
                ? null
                : cb.equal(cb.lower(root.get("username")), username.trim().toLowerCase());
    }

    public static Specification<AppUser> hasSystemRole(String role) {
        return (root, query, cb) -> role == null || role.isBlank()
                ? null
                : cb.equal(root.get("systemRole"), SystemRole.valueOf(role.trim().toUpperCase()));
    }

    public static Specification<AppUser> hasStatus(String status) {
        return (root, query, cb) -> status == null || status.isBlank()
                ? null
                : cb.equal(root.get("status"), UserStatus.valueOf(status.trim().toUpperCase()));
    }

    public static Specification<AppUser> hasWorkspaceId(java.util.UUID workspaceId) {
        return (root, query, cb) -> {
            if (workspaceId == null) {
                return null;
            }
            query.distinct(true);
            var membershipSubquery = query.subquery(java.util.UUID.class);
            var membership = membershipSubquery.from(WorkspaceMembership.class);
            membershipSubquery.select(membership.get("user").get("id"))
                    .where(
                            cb.equal(membership.get("user").get("id"), root.get("id")),
                            cb.equal(membership.get("workspace").get("id"), workspaceId),
                            cb.equal(membership.get("status"), MembershipStatus.ACTIVE)
                    );
            return cb.exists(membershipSubquery);
        };
    }

    public static Specification<AppUser> hasTagId(java.util.UUID tagId) {
        return (root, query, cb) -> {
            if (tagId == null) {
                return null;
            }
            query.distinct(true);
            var tagSubquery = query.subquery(java.util.UUID.class);
            var tagAssignment = tagSubquery.from(UserTagAssignment.class);
            tagSubquery.select(tagAssignment.get("targetUser").get("id"))
                    .where(
                            cb.equal(tagAssignment.get("targetUser").get("id"), root.get("id")),
                            cb.equal(tagAssignment.get("tag").get("id"), tagId),
                            cb.equal(tagAssignment.get("assignmentStatus"), AssignmentStatus.ACTIVE)
                    );
            return cb.exists(tagSubquery);
        };
    }

    public static Specification<AppUser> createdAtGreaterThanOrEqual(OffsetDateTime value) {
        return (root, query, cb) -> value == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), value);
    }

    public static Specification<AppUser> createdAtLessThanOrEqual(OffsetDateTime value) {
        return (root, query, cb) -> value == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), value);
    }

    public static Specification<AppUser> notDeletedByDefault(Boolean includeDeleted) {
        return (root, query, cb) -> Boolean.TRUE.equals(includeDeleted)
                ? null
                : cb.notEqual(root.get("status"), UserStatus.DELETED);
    }
}
