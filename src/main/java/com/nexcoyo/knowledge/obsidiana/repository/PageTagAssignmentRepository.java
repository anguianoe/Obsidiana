package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.PageTagAssignment;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageTagAssignmentRepository extends JpaRepository<PageTagAssignment, UUID> {
    List<PageTagAssignment> findAllByPageIdAndWorkspaceIdAndAssignmentStatus(UUID pageId, UUID workspaceId, AssignmentStatus assignmentStatus);
    List<PageTagAssignment> findAllByPageIdAndWorkspaceIdAndAssignmentStatusAndCreatedById(UUID pageId, UUID workspaceId, AssignmentStatus assignmentStatus, UUID createdById);
    List<PageTagAssignment> findAllByTagIdAndAssignmentStatus(UUID tagId, AssignmentStatus assignmentStatus);
    Optional< PageTagAssignment > findByPageIdAndWorkspaceIdAndTagId( UUID pageId, UUID workspaceId, UUID tagId);
}
