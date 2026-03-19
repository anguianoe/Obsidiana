package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.UserProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID>, JpaSpecificationExecutor<UserProfile> {
    Optional<UserProfile> findByUserId(UUID userId);

    @EntityGraph(attributePaths = {"user", "avatarAsset"})
    Optional<UserProfile> findDetailedByUserId(UUID userId);

    @EntityGraph(attributePaths = {"avatarAsset"})
    List<UserProfile> findAllByUserIdIn(Collection<UUID> userIds);
}
