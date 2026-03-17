package com.nexcoyo.knowledge.obsidiana.repository;

import com.nexcoyo.knowledge.obsidiana.entity.RefreshTokenEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository< RefreshTokenEntity, UUID >
{
    @Lock( LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshTokenEntity> findByTokenHash( String tokenHash);
    @Query("""
     select rt from RefreshTokenEntity rt
     where rt.user.id = :userId
       and rt.revokedAt is null
       and rt.expiresAt > :now
     order by rt.issuedAt asc
  """)
    List<RefreshTokenEntity> findActiveByUserOldestFirst(
            @Param("userId") UUID userId,
            @Param("now") Instant now
    );
}

