package com.nexcoyo.knowledge.obsidiana.service.specification;

import com.nexcoyo.knowledge.obsidiana.entity.PageComment;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.CommentSearchCriteria;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

public final class PageCommentSpecifications {

    private PageCommentSpecifications() {
    }

    public static Specification< PageComment > byCriteria( CommentSearchCriteria criteria) {
        return Specification.allOf(
            equalsPath("page.id", criteria.getPageId()),
            equalsPath("workspace.id", criteria.getWorkspaceId()),
            equalsPath("authorUser.id", criteria.getAuthorUserId()),
            parent(criteria.getParentCommentId(), criteria.getOnlyRoots()),
            status(criteria.getStatus()),
            text(criteria.getText())
        );
    }

    private static Specification<PageComment> equalsPath(String path, Object value) {
        return (root, query, cb) -> {
            if (value == null) return null;
            Path<?> current = root;
            for (String part : path.split("\\.")) {
                current = current.get(part);
            }
            return cb.equal(current, value);
        };
    }

    private static Specification<PageComment> parent(java.util.UUID parentId, Boolean onlyRoots) {
        return (root, query, cb) -> {
            if (Boolean.TRUE.equals(onlyRoots)) return cb.isNull(root.get("parentComment"));
            if (parentId == null) return null;
            return cb.equal(root.get("parentComment").get("id"), parentId);
        };
    }

    private static Specification<PageComment> status(Enum<?> status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("commentStatus"), status);
    }

    private static Specification<PageComment> text(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) return null;
            return cb.like(cb.lower(root.get("body")), "%" + text.toLowerCase() + "%");
        };
    }
}
