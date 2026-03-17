package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorkspaceMembershipRepository extends JpaRepository<WorkspaceMembership, UUID>, JpaSpecificationExecutor< WorkspaceMembership > {
    Optional<WorkspaceMembership> findByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);
    boolean existsByWorkspaceIdAndUserIdAndStatus(UUID workspaceId, UUID userId, MembershipStatus status);
    List<WorkspaceMembership> findAllByWorkspaceIdAndStatus(UUID workspaceId, MembershipStatus status);
    List<WorkspaceMembership> findAllByUserIdAndStatus(UUID userId, MembershipStatus status);
}
