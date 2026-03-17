package com.nexcoyo.knowledge.obsidiana.repository;


import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

import com.nexcoyo.knowledge.obsidiana.entity.UserSession;
import com.nexcoyo.knowledge.obsidiana.util.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID>, JpaSpecificationExecutor<UserSession> {
    List< UserSession > findByUserId( UUID userId);

    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update UserSession s
           set s.sessionStatus = :revokedStatus,
               s.revokedAt = :revokedAt
         where s.user.id = :actorUserId
           and (:currentSessionId is null or s.id <> :currentSessionId)
           and s.sessionStatus = :activeStatus
           and s.revokedAt is null
           and (s.expiresAt is null or s.expiresAt > :revokedAt)
    """)
    long bulkRevokeOtherActiveSessions(
            @Param("actorUserId") UUID actorUserId,
            @Param("currentSessionId") UUID currentSessionId,
            @Param("revokedAt") OffsetDateTime revokedAt,
            @Param("activeStatus") SessionStatus activeStatus,
            @Param("revokedStatus") SessionStatus revokedStatus
    );

    default long bulkRevokeOtherActiveSessions(UUID actorUserId, UUID currentSessionId, OffsetDateTime revokedAt) {
        return bulkRevokeOtherActiveSessions(actorUserId, currentSessionId, revokedAt, SessionStatus.ACTIVE, SessionStatus.REVOKED);
    }
}
