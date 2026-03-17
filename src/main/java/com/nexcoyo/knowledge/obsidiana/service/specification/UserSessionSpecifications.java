package com.nexcoyo.knowledge.obsidiana.service.specification;


import java.time.OffsetDateTime;

import com.nexcoyo.knowledge.obsidiana.dto.request.UserSessionSearchRequest;
import com.nexcoyo.knowledge.obsidiana.entity.UserSession;
import com.nexcoyo.knowledge.obsidiana.util.enums.SessionStatus;
import org.springframework.data.jpa.domain.Specification;

public final class UserSessionSpecifications {

    private UserSessionSpecifications() {
    }

    public static Specification< UserSession > from( UserSessionSearchRequest request) {
        if (request == null) {
            return (root, query, cb) -> null;
        }
        return Specification.allOf(
                hasUserId(request.userId()),
                hasStatus(request.sessionStatus()),
                containsText(request.text()),
                loginAtGreaterThanOrEqual(request.loginFrom()),
                loginAtLessThanOrEqual(request.loginTo()),
                expiresBefore(request.expiresBefore()),
                activeOnly(request.activeOnly())
        );
    }

    public static Specification<UserSession> hasUserId(java.util.UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<UserSession> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) {
                return null;
            }
            return cb.equal(root.get("sessionStatus"), SessionStatus.valueOf(status.trim().toUpperCase()));
        };
    }

    public static Specification<UserSession> containsText(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) {
                return null;
            }
            String like = "%" + text.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("ipAddress")), like),
                    cb.like(cb.lower(root.get("userAgent")), like),
                    cb.like(cb.lower(root.get("deviceType")), like),
                    cb.like(cb.lower(root.get("osName")), like),
                    cb.like(cb.lower(root.get("browserName")), like),
                    cb.like(cb.lower(root.get("cityName")), like),
                    cb.like(cb.lower(root.get("regionName")), like),
                    cb.like(cb.lower(root.get("countryName")), like)
            );
        };
    }

    public static Specification<UserSession> loginAtGreaterThanOrEqual(OffsetDateTime value) {
        return (root, query, cb) -> value == null ? null : cb.greaterThanOrEqualTo(root.get("loginAt"), value);
    }

    public static Specification<UserSession> loginAtLessThanOrEqual(OffsetDateTime value) {
        return (root, query, cb) -> value == null ? null : cb.lessThanOrEqualTo(root.get("loginAt"), value);
    }

    public static Specification<UserSession> expiresBefore(OffsetDateTime value) {
        return (root, query, cb) -> value == null ? null : cb.lessThanOrEqualTo(root.get("expiresAt"), value);
    }

    public static Specification<UserSession> activeOnly(Boolean activeOnly) {
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(activeOnly)) {
                return null;
            }
            OffsetDateTime now = OffsetDateTime.now();
            return cb.and(
                    cb.equal(root.get("sessionStatus"), SessionStatus.ACTIVE),
                    cb.or(cb.isNull(root.get("expiresAt")), cb.greaterThan(root.get("expiresAt"), now))
            );
        };
    }
}
