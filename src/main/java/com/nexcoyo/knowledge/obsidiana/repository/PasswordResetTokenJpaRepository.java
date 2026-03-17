package com.nexcoyo.knowledge.obsidiana.repository;

import com.nexcoyo.knowledge.obsidiana.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenEntity, UUID >
{
    Optional< PasswordResetTokenEntity > findByTokenHash( String tokenHash);
}


