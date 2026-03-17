package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import com.nexcoyo.knowledge.obsidiana.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PasswordResetTokenRepository extends JpaRepository< PasswordResetToken, UUID>, JpaSpecificationExecutor<PasswordResetToken> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    List<PasswordResetToken> findByUserId(UUID userId);

}
