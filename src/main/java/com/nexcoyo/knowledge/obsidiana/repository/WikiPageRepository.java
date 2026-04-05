package com.nexcoyo.knowledge.obsidiana.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.projection.PageTreeNodeProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WikiPageRepository extends JpaRepository<WikiPage, UUID>, JpaSpecificationExecutor<WikiPage> {

    Optional< WikiPage > findByPublicUuid( UUID publicUuid);

    @Query("""
        select count(p) > 0
        from WikiPage p
        where p.id = :pageId
          and (
            p.ownerUser.id = :userId
            or exists (
              select pwl from PageWorkspaceLink pwl
              join WorkspaceMembership wm on wm.workspace.id = pwl.workspace.id
              where pwl.page.id = p.id
                and wm.user.id = :userId
                and wm.status = com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus.ACTIVE
            )
          )
    """)
    boolean existsAccessibleByIdAndUserId(@Param("pageId") UUID pageId, @Param("userId") UUID userId);

    @Query("""
        select count(p) > 0
        from WikiPage p
        join PageWorkspaceLink pwl on pwl.page.id = p.id
        where pwl.workspace.id = :workspaceId
          and (
            p.ownerUser.id = :userId
            or exists (
              select wm from WorkspaceMembership wm
              where wm.workspace.id = pwl.workspace.id
                and wm.user.id = :userId
                and wm.status = com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus.ACTIVE
            )
          )
    """)
    boolean existsAccessibleByWorkspaceIdAndUserId(@Param("workspaceId") UUID workspaceId, @Param("userId") UUID userId);

    @Query("""
        select p
        from WikiPage p
        join PageWorkspaceLink pwl on pwl.page.id = p.id
        left join PageTagAssignment pta on pta.page.id = p.id and pta.workspace.id = pwl.workspace.id
        where (
              p.ownerUser.id = :userId
              or exists (
                    select wm.id
                    from WorkspaceMembership wm
                    where wm.workspace.id = pwl.workspace.id
                      and wm.user.id = :userId
                      and wm.status = com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus.ACTIVE
              )
          )
          and p.pageStatus = com.nexcoyo.knowledge.obsidiana.util.enums.PageStatus.ACTIVE
          and (:workspaceId is null or pwl.workspace.id = :workspaceId)
          and (:tagId is null or pta.tag.id = :tagId)
          and (
                  cast(:searchText as string) is null
                  or lower(p.title) like lower(concat('%', cast(:searchText as string), '%'))
                  or lower(p.slug) like lower(concat('%', cast(:searchText as string), '%'))
          )
        order by p.updatedAt desc
    """)
    Page<WikiPage> searchAccessiblePages(
        @Param("userId") UUID userId,
        @Param("workspaceId") UUID workspaceId,
        @Param("tagId") UUID tagId,
        @Param("searchText") String searchText,
        Pageable pageable
    );

    @Query("""
        select p.id as pageId,
               p.title as title,
               p.slug as slug,
               coalesce(ph.sortOrder, 0) as sortOrder,
               (select count(ph2.id) from PageHierarchy ph2 where ph2.parentPage.id = p.id and ph2.workspace.id = :workspaceId) as childCount
        from WikiPage p
        join PageWorkspaceLink pwl on pwl.page.id = p.id and pwl.workspace.id = :workspaceId
        left join PageHierarchy ph on ph.childPage.id = p.id and ph.workspace.id = :workspaceId
        where p.pageStatus = com.nexcoyo.knowledge.obsidiana.util.enums.PageStatus.ACTIVE
          and ((:parentPageId is null and ph.id is null) or (ph.parentPage.id = :parentPageId))
        order by coalesce(ph.sortOrder, 0) asc, p.title asc
    """)
    List< PageTreeNodeProjection > findTreeNodes( @Param("workspaceId") UUID workspaceId, @Param("parentPageId") UUID parentPageId);
}
