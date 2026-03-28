package com.nexcoyo.knowledge.obsidiana.facade.support;

import java.util.Objects;
import java.util.UUID;

/**
 * Encodes caller identity and access scope without using null-sentinel contracts.
 */
public sealed interface AccessContext permits AccessContext.User, AccessContext.Admin {

    UUID actorUserId();

    boolean requiresPageAccessCheck();

    static AccessContext user(UUID userId) {
        return new User(userId);
    }

    static AccessContext admin(UUID adminUserId) {
        return new Admin(adminUserId);
    }

    record User(UUID actorUserId) implements AccessContext {
        public User {
            Objects.requireNonNull(actorUserId, "actorUserId is required");
        }

        @Override
        public boolean requiresPageAccessCheck() {
            return true;
        }
    }

    record Admin(UUID actorUserId) implements AccessContext {
        public Admin {
            Objects.requireNonNull(actorUserId, "actorUserId is required");
        }

        @Override
        public boolean requiresPageAccessCheck() {
            return false;
        }
    }
}

