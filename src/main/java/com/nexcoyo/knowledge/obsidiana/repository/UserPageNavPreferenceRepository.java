package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.UserPageNavPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPageNavPreferenceRepository extends JpaRepository< UserPageNavPreference, UUID> {
    List<UserPageNavPreference> findAllByUserIdAndWorkspaceIdOrderByPinnedDescSortOrderAsc(UUID userId, UUID workspaceId);
    Optional<UserPageNavPreference> findByUserIdAndWorkspaceIdAndPageId(UUID userId, UUID workspaceId, UUID pageId);
}
