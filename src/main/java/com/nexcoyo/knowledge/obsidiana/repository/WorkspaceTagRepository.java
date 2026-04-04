package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceTag;import com.nexcoyo.knowledge.obsidiana.util.enums.TagStatus;import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceTagRepository extends JpaRepository<WorkspaceTag, UUID> {
    List<WorkspaceTag> findAllByWorkspaceIdAndTagStatusOrderByNameAsc(UUID workspaceId, TagStatus tagStatus);
    List<WorkspaceTag> findAllByWorkspaceIdAndTagStatusAndCreatedByIdOrderByNameAsc(UUID workspaceId, TagStatus tagStatus, UUID createdById);
    Optional<WorkspaceTag> findByWorkspaceIdAndNameIgnoreCase(UUID workspaceId, String name);
}
