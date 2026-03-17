package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.UserTagAssignment;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserTagAssignmentRepository extends JpaRepository< UserTagAssignment, UUID> {

    @Query("""
            select uta
            from UserTagAssignment uta
            join fetch uta.targetUser tu
            join fetch uta.workspace w
            join fetch uta.tag t
            left join fetch uta.createdBy cb
            where tu.id = :userId
            order by uta.createdAt desc
            """)
    List<UserTagAssignment> findAllDetailedByTargetUserId(@Param("userId") UUID userId);

    @Query("""
            select uta
            from UserTagAssignment uta
            join fetch uta.targetUser tu
            join fetch uta.workspace w
            join fetch uta.tag t
            left join fetch uta.createdBy cb
            where tu.id = :userId
              and w.id = :workspaceId
            order by uta.createdAt desc
            """)
    List<UserTagAssignment> findAllByTargetUserIdAndWorkspaceId(@Param("userId") UUID userId, @Param("workspaceId") UUID workspaceId);

    @Query("""
            select uta
            from UserTagAssignment uta
            join fetch uta.targetUser tu
            join fetch uta.workspace w
            join fetch uta.tag t
            left join fetch uta.createdBy cb
            where tu.id = :userId
              and uta.assignmentStatus = :status
            order by uta.createdAt desc
            """)
    List<UserTagAssignment> findAllByTargetUserIdAndAssignmentStatus(@Param("userId") UUID userId, @Param("status") AssignmentStatus status);

    @Query("""
            select uta
            from UserTagAssignment uta
            join fetch uta.targetUser tu
            join fetch uta.workspace w
            join fetch uta.tag t
            left join fetch uta.createdBy cb
            where tu.id = :userId
              and w.id = :workspaceId
              and uta.assignmentStatus = :status
            order by uta.createdAt desc
            """)
    List<UserTagAssignment> findAllByTargetUserIdAndWorkspaceIdAndAssignmentStatus(
            @Param("userId") UUID userId,
            @Param("workspaceId") UUID workspaceId,
            @Param("status") AssignmentStatus status
    );

    @Query("""
            select uta
            from UserTagAssignment uta
            join fetch uta.targetUser tu
            join fetch uta.workspace w
            join fetch uta.tag t
            left join fetch uta.createdBy cb
            where tu.id = :userId
              and w.id = :workspaceId
              and t.id = :tagId
            """)
    Optional<UserTagAssignment> findByTargetUserIdAndWorkspaceIdAndTagId(
            @Param("userId") UUID userId,
            @Param("workspaceId") UUID workspaceId,
            @Param("tagId") UUID tagId
    );

    default List<UserTagAssignment> findAllByTargetUserId(UUID userId) {
        return findAllDetailedByTargetUserId(userId);
    }
}
