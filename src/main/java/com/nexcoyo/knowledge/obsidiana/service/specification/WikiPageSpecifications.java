package com.nexcoyo.knowledge.obsidiana.service.specification;

import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.PageHierarchy;
import com.nexcoyo.knowledge.obsidiana.entity.PageTagAssignment;
import com.nexcoyo.knowledge.obsidiana.entity.PageWorkspaceLink;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.WikiPageSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import org.springframework.data.jpa.domain.Specification;

public final class WikiPageSpecifications {

    private WikiPageSpecifications() {
    }

    public static Specification< WikiPage > byCriteria( WikiPageSearchCriteria criteria) {
        return Specification.allOf(
            ownerUser(criteria.getOwnerUserId()),
            workspace(criteria.getWorkspaceId()),
            tag(criteria.getTagId(), criteria.getWorkspaceId()),
            text(criteria.getText()),
            pageStatus(criteria.getPageStatus()),
            editMode(criteria.getEditMode()),
            encrypted(criteria.getEncrypted()),
            publicable(criteria.getPublicable()),
            rootPages(criteria.getOnlyRootPages(), criteria.getWorkspaceId()),
            childOf(criteria.getParentPageId(), criteria.getWorkspaceId())
        );
    }

    public static Specification<WikiPage> accessibleToUser(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) return null;

            var membershipSubquery = query.subquery(UUID.class);
            var membership = membershipSubquery.from(WorkspaceMembership.class);
            var pageWorkspaceLink = membershipSubquery.from(PageWorkspaceLink.class);

            membershipSubquery.select(pageWorkspaceLink.get("page").get("id"));
            membershipSubquery.where(
                cb.equal(pageWorkspaceLink.get("page").get("id"), root.get("id")),
                cb.equal(pageWorkspaceLink.get("workspace").get("id"), membership.get("workspace").get("id")),
                cb.equal(membership.get("user").get("id"), userId),
                cb.equal(membership.get("status"), MembershipStatus.ACTIVE)
            );

            return cb.or(
                cb.equal(root.get("ownerUser").get("id"), userId),
                cb.exists(membershipSubquery)
            );
        };
    }

    public static Specification<WikiPage> ownerUser(UUID ownerUserId) {
        return (root, query, cb) -> ownerUserId == null ? null : cb.equal(root.get("ownerUser").get("id"), ownerUserId);
    }

    public static Specification<WikiPage> workspace(UUID workspaceId) {
        return (root, query, cb) -> {
            if (workspaceId == null) return null;
            var sq = query.subquery(UUID.class);
            var link = sq.from( PageWorkspaceLink.class);
            sq.select(link.get("page").get("id"));
            sq.where(cb.equal(link.get("page").get("id"), root.get("id")), cb.equal(link.get("workspace").get("id"), workspaceId));
            return cb.exists(sq);
        };
    }

    public static Specification<WikiPage> tag(UUID tagId, UUID workspaceId) {
        return (root, query, cb) -> {
            if (tagId == null) return null;
            var sq = query.subquery(UUID.class);
            var tag = sq.from( PageTagAssignment.class);
            sq.select(tag.get("page").get("id"));
            if (workspaceId == null) {
                sq.where(cb.equal(tag.get("page").get("id"), root.get("id")), cb.equal(tag.get("tag").get("id"), tagId));
            } else {
                sq.where(cb.equal(tag.get("page").get("id"), root.get("id")), cb.equal(tag.get("tag").get("id"), tagId), cb.equal(tag.get("workspace").get("id"), workspaceId));
            }
            return cb.exists(sq);
        };
    }

    public static Specification<WikiPage> text(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) return null;
            String pattern = "%" + text.toLowerCase() + "%";
            return cb.or(cb.like(cb.lower(root.get("title")), pattern), cb.like(cb.lower(root.get("slug")), pattern));
        };
    }

    public static Specification<WikiPage> pageStatus(Enum<?> pageStatus) {
        return (root, query, cb) -> pageStatus == null ? null : cb.equal(root.get("pageStatus"), pageStatus);
    }

    public static Specification<WikiPage> editMode(Enum<?> editMode) {
        return (root, query, cb) -> editMode == null ? null : cb.equal(root.get("editMode"), editMode);
    }

    public static Specification<WikiPage> encrypted(Boolean encrypted) {
        return (root, query, cb) -> encrypted == null ? null : cb.equal(root.get("isEncrypted"), encrypted);
    }

    public static Specification<WikiPage> publicable(Boolean publicable) {
        return (root, query, cb) -> publicable == null ? null : cb.equal(root.get("isPublicable"), publicable);
    }

    public static Specification<WikiPage> rootPages(Boolean onlyRootPages, UUID workspaceId) {
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(onlyRootPages) || workspaceId == null) return null;
            var sq = query.subquery(UUID.class);
            var hierarchy = sq.from( PageHierarchy.class);
            sq.select(hierarchy.get("childPage").get("id"));
            sq.where(cb.equal(hierarchy.get("childPage").get("id"), root.get("id")), cb.equal(hierarchy.get("workspace").get("id"), workspaceId));
            return cb.not(cb.exists(sq));
        };
    }

    public static Specification<WikiPage> childOf(UUID parentPageId, UUID workspaceId) {
        return (root, query, cb) -> {
            if (parentPageId == null || workspaceId == null) return null;
            var sq = query.subquery(UUID.class);
            var hierarchy = sq.from(PageHierarchy.class);
            sq.select(hierarchy.get("childPage").get("id"));
            sq.where(
                cb.equal(hierarchy.get("childPage").get("id"), root.get("id")),
                cb.equal(hierarchy.get("parentPage").get("id"), parentPageId),
                cb.equal(hierarchy.get("workspace").get("id"), workspaceId)
            );
            return cb.exists(sq);
        };
    }
}
