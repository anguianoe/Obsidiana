package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.PageTagAssignment;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceTag;
import com.nexcoyo.knowledge.obsidiana.repository.AppUserRepository;
import com.nexcoyo.knowledge.obsidiana.repository.PageTagAssignmentRepository;
import com.nexcoyo.knowledge.obsidiana.repository.PageWorkspaceLinkRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WikiPageRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceMembershipRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceTagRepository;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssignmentStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.SystemRole;
import com.nexcoyo.knowledge.obsidiana.util.enums.TagStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.UserStatus;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock private WorkspaceTagRepository workspaceTagRepository;
    @Mock private PageTagAssignmentRepository pageTagAssignmentRepository;
    @Mock private WikiPageRepository wikiPageRepository;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private WorkspaceMembershipRepository workspaceMembershipRepository;
    @Mock private PageWorkspaceLinkRepository pageWorkspaceLinkRepository;
    @Mock private AppUserRepository appUserRepository;

    private TagServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TagServiceImpl(
            workspaceTagRepository,
            pageTagAssignmentRepository,
            wikiPageRepository,
            workspaceRepository,
            workspaceMembershipRepository,
            pageWorkspaceLinkRepository,
            appUserRepository
        );
    }

    @Test
    void getActiveTagsForUserReturnsAllWhenWorkspaceMembershipIsActive() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Workspace workspace = workspace(workspaceId, UUID.randomUUID());
        WorkspaceTag tag = new WorkspaceTag();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceMembershipRepository.existsByWorkspaceIdAndUserIdAndStatus(workspaceId, userId, MembershipStatus.ACTIVE)).thenReturn(true);
        when(workspaceTagRepository.findAllByWorkspaceIdAndTagStatusOrderByNameAsc(workspaceId, TagStatus.ACTIVE)).thenReturn(List.of(tag));

        List<WorkspaceTag> result = service.getActiveTags(workspaceId, userId);

        assertThat(result).containsExactly(tag);
    }

    @Test
    void getActiveTagsForUserReturnsCreatedByOnlyWhenNoRelation() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Workspace workspace = workspace(workspaceId, UUID.randomUUID());
        WorkspaceTag tag = new WorkspaceTag();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceMembershipRepository.existsByWorkspaceIdAndUserIdAndStatus(workspaceId, userId, MembershipStatus.ACTIVE)).thenReturn(false);
        when(wikiPageRepository.existsAccessibleByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(false);
        when(workspaceTagRepository.findAllByWorkspaceIdAndTagStatusAndCreatedByIdOrderByNameAsc(workspaceId, TagStatus.ACTIVE, userId)).thenReturn(List.of(tag));

        List<WorkspaceTag> result = service.getActiveTags(workspaceId, userId);

        assertThat(result).containsExactly(tag);
        verify(workspaceTagRepository, never()).findAllByWorkspaceIdAndTagStatusOrderByNameAsc(workspaceId, TagStatus.ACTIVE);
    }

    @Test
    void saveTagForUserCreateRejectsWhenNoWorkspaceRelation() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Workspace workspace = workspace(workspaceId, UUID.randomUUID());
        WorkspaceTag workspaceTag = new WorkspaceTag();
        workspaceTag.setWorkspace(workspace);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceMembershipRepository.existsByWorkspaceIdAndUserIdAndStatus(workspaceId, userId, MembershipStatus.ACTIVE)).thenReturn(false);
        when(wikiPageRepository.existsAccessibleByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(false);

        assertThatThrownBy(() -> service.saveTag(workspaceTag, userId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("access denied");
    }

    @Test
    void saveTagForUserUpdateAllowsCreatorWithoutMembership() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        Workspace workspace = workspace(workspaceId, UUID.randomUUID());
        WorkspaceTag existing = new WorkspaceTag();
        existing.setId(tagId);
        existing.setWorkspace(workspace);
        existing.setName("old");
        existing.setTagStatus(TagStatus.ACTIVE);
        existing.setCreatedBy(user(userId));

        WorkspaceTag incoming = new WorkspaceTag();
        incoming.setId(tagId);
        incoming.setWorkspace(workspace);
        incoming.setName("new");
        incoming.setTagStatus(TagStatus.INACTIVE);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceMembershipRepository.existsByWorkspaceIdAndUserIdAndStatus(workspaceId, userId, MembershipStatus.ACTIVE)).thenReturn(false);
        when(wikiPageRepository.existsAccessibleByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(false);
        when(workspaceTagRepository.findById(tagId)).thenReturn(Optional.of(existing));
        when(workspaceTagRepository.save(any(WorkspaceTag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkspaceTag result = service.saveTag(incoming, userId);

        assertThat(result.getName()).isEqualTo("new");
        assertThat(result.getTagStatus()).isEqualTo(TagStatus.INACTIVE);
    }

    @Test
    void assignTagForUserRequiresWorkspaceOrPageRelation() {
        UUID pageId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());
        when(wikiPageRepository.existsAccessibleByIdAndUserId(pageId, userId)).thenReturn(false);

        assertThatThrownBy(() -> service.assignTag(pageId, workspaceId, tagId, userId, userId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("access denied");
    }

    @Test
    void getPageAssignmentsForUserReturnsCreatedByOnlyWhenNoRelation() {
        UUID pageId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Workspace workspace = workspace(workspaceId, UUID.randomUUID());
        PageTagAssignment ownAssignment = new PageTagAssignment();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceMembershipRepository.existsByWorkspaceIdAndUserIdAndStatus(workspaceId, userId, MembershipStatus.ACTIVE)).thenReturn(false);
        when(wikiPageRepository.existsAccessibleByIdAndUserId(pageId, userId)).thenReturn(false);
        when(pageTagAssignmentRepository.findAllByPageIdAndWorkspaceIdAndAssignmentStatusAndCreatedById(pageId, workspaceId, AssignmentStatus.ACTIVE, userId))
            .thenReturn(List.of(ownAssignment));

        List<PageTagAssignment> result = service.getPageAssignments(pageId, workspaceId, userId);

        assertThat(result).containsExactly(ownAssignment);
    }

    @Test
    void saveTagForUserCreateAllowsWhenPageRelationExists() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Workspace workspace = workspace(workspaceId, UUID.randomUUID());
        WorkspaceTag incoming = new WorkspaceTag();
        incoming.setWorkspace(workspace);
        incoming.setName("new");
        incoming.setTagStatus(TagStatus.ACTIVE);
        AppUser actor = user(userId);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceMembershipRepository.existsByWorkspaceIdAndUserIdAndStatus(workspaceId, userId, MembershipStatus.ACTIVE)).thenReturn(false);
        when(wikiPageRepository.existsAccessibleByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(true);
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(actor));
        when(workspaceTagRepository.save(any(WorkspaceTag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkspaceTag result = service.saveTag(incoming, userId);

        assertThat(result.getCreatedBy()).isSameAs(actor);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void assignTagForUserAllowsWhenPageRelationExists() {
        UUID pageId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Workspace workspace = workspace(workspaceId, UUID.randomUUID());
        WikiPage page = new WikiPage();
        page.setId(pageId);
        WorkspaceTag tag = new WorkspaceTag();
        tag.setId(tagId);
        tag.setWorkspace(workspace);
        AppUser actor = user(userId);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceMembershipRepository.existsByWorkspaceIdAndUserIdAndStatus(workspaceId, userId, MembershipStatus.ACTIVE)).thenReturn(false);
        when(wikiPageRepository.existsAccessibleByIdAndUserId(pageId, userId)).thenReturn(true);
        when(wikiPageRepository.findById(pageId)).thenReturn(Optional.of(page));
        when(workspaceTagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(pageWorkspaceLinkRepository.existsByPageIdAndWorkspaceId(pageId, workspaceId)).thenReturn(true);
        when(pageTagAssignmentRepository.findByPageIdAndWorkspaceIdAndTagId(pageId, workspaceId, tagId)).thenReturn(Optional.empty());
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(actor));
        when(pageTagAssignmentRepository.save(any(PageTagAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PageTagAssignment result = service.assignTag(pageId, workspaceId, tagId, userId, userId);

        assertThat(result.getCreatedBy()).isSameAs(actor);
        assertThat(result.getWorkspace()).isSameAs(workspace);
        assertThat(result.getPage()).isSameAs(page);
    }

    @Test
    void getPageAssignmentsForUserReturnsAllWhenPageRelationExists() {
        UUID pageId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Workspace workspace = workspace(workspaceId, UUID.randomUUID());
        PageTagAssignment assignment = new PageTagAssignment();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceMembershipRepository.existsByWorkspaceIdAndUserIdAndStatus(workspaceId, userId, MembershipStatus.ACTIVE)).thenReturn(false);
        when(wikiPageRepository.existsAccessibleByIdAndUserId(pageId, userId)).thenReturn(true);
        when(pageTagAssignmentRepository.findAllByPageIdAndWorkspaceIdAndAssignmentStatus(pageId, workspaceId, AssignmentStatus.ACTIVE))
            .thenReturn(List.of(assignment));

        List<PageTagAssignment> result = service.getPageAssignments(pageId, workspaceId, userId);

        assertThat(result).containsExactly(assignment);
        verify(pageTagAssignmentRepository, never())
            .findAllByPageIdAndWorkspaceIdAndAssignmentStatusAndCreatedById(pageId, workspaceId, AssignmentStatus.ACTIVE, userId);
    }

    private static Workspace workspace(UUID id, UUID creatorId) {
        Workspace workspace = new Workspace();
        workspace.setId(id);
        workspace.setCreatedBy(user(creatorId));
        return workspace;
    }

    private static AppUser user(UUID id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail(id + "@mail.test");
        user.setUsername("u-" + id);
        user.setPasswordHash("hash");
        user.setSystemRole(SystemRole.USER);
        user.setRoles("USER");
        user.setStatus(UserStatus.ACTIVE);
        user.setHasCompletedOnboarding(Boolean.TRUE);
        user.setOnboardingVersion("1.0");
        return user;
    }
}



