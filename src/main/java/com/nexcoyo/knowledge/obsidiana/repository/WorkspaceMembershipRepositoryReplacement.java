package com.nexcoyo.knowledge.obsidiana.repository;

/*
 Replace the previous WorkspaceMembershipRepository with the version below if you want
 eager fetch helpers for the new Users module. Kept as a reference file to avoid forcing
 a duplicate class name into your project if you already copied the old repository.

 public interface WorkspaceMembershipRepository extends JpaRepository<WorkspaceMembership, UUID>, JpaSpecificationExecutor<WorkspaceMembership> {

     Optional<WorkspaceMembership> findByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);
     boolean existsByWorkspaceIdAndUserIdAndStatus(UUID workspaceId, UUID userId, MembershipStatus status);
     List<WorkspaceMembership> findAllByWorkspaceIdAndStatus(UUID workspaceId, MembershipStatus status);
     List<WorkspaceMembership> findAllByUserIdAndStatus(UUID userId, MembershipStatus status);

     @Query("""
             select wm
             from WorkspaceMembership wm
             join fetch wm.workspace w
             join fetch wm.user u
             left join fetch wm.createdBy cb
             where u.id = :userId
             order by w.name asc
             """)
     List<WorkspaceMembership> findAllByUserId(@Param("userId") UUID userId);
 }
 */
public final class WorkspaceMembershipRepositoryReplacement {
    private WorkspaceMembershipRepositoryReplacement() {
    }
}
