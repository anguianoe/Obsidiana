package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.UUID;
import java.util.Optional;

import com.nexcoyo.knowledge.obsidiana.entity.UserPreference;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UUID>, JpaSpecificationExecutor<UserPreference> {
    Optional<UserPreference> findByUserId(UUID userId);

    @EntityGraph(attributePaths = {"user"})
    Optional<UserPreference> findDetailedByUserId(UUID userId);
}
