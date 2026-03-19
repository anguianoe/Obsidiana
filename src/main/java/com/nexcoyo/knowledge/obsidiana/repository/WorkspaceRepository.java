package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.projection.WorkspaceSummaryProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID>, JpaSpecificationExecutor< Workspace > {

    Optional<Workspace> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"createdBy", "approvedBy"})
    @NonNull
    Optional<Workspace> findById(@NonNull UUID id);

    @EntityGraph(attributePaths = {"createdBy", "approvedBy"})
    Optional<Workspace> findByIdAndCreatedById(UUID workspaceId, UUID userId);

    @Query("""
        select w.id as workspaceId,
               w.name as workspaceName,
               w.slug as workspaceSlug,
               w.kind as kind,
               w.status as status,
               w.approvalStatus as approvalStatus,
               wm.role as membershipRole,
               wm.joinedAt as joinedAt
        from WorkspaceMembership wm
        join wm.workspace w
        where wm.user.id = :userId
          and wm.status = com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus.ACTIVE
        order by w.name asc
    """)
    List< WorkspaceSummaryProjection > findAccessibleWorkspaceSummaries( @Param("userId") UUID userId);
}
