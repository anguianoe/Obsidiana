package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.UUID;
import java.util.Optional;

import com.nexcoyo.knowledge.obsidiana.entity.UserProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID>, JpaSpecificationExecutor<UserProfile> {
    Optional<UserProfile> findByUserId(UUID userId);

    @EntityGraph(attributePaths = {"user", "avatarAsset"})
    Optional<UserProfile> findDetailedByUserId(UUID userId);
}
