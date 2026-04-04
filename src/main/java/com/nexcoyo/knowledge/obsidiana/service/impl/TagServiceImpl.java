package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.PageTagAssignment;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceTag;
import com.nexcoyo.knowledge.obsidiana.repository.AppUserRepository;
import com.nexcoyo.knowledge.obsidiana.repository.PageWorkspaceLinkRepository;
import com.nexcoyo.knowledge.obsidiana.repository.PageTagAssignmentRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WikiPageRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceMembershipRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceTagRepository;
import com.nexcoyo.knowledge.obsidiana.service.TagService;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssignmentStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.TagStatus;
import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {

    private final WorkspaceTagRepository workspaceTagRepository;
    private final PageTagAssignmentRepository pageTagAssignmentRepository;
    private final WikiPageRepository wikiPageRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMembershipRepository workspaceMembershipRepository;
    private final PageWorkspaceLinkRepository pageWorkspaceLinkRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public List< WorkspaceTag > getActiveTags( UUID workspaceId) {
        return workspaceTagRepository.findAllByWorkspaceIdAndTagStatusOrderByNameAsc(workspaceId, TagStatus.ACTIVE);
    }

    @Override
    public List<WorkspaceTag> getActiveTags(UUID workspaceId, UUID userId) {
        if (canAccessWorkspace(workspaceId, userId) || hasAccessiblePageInWorkspace(workspaceId, userId)) {
            return workspaceTagRepository.findAllByWorkspaceIdAndTagStatusOrderByNameAsc(workspaceId, TagStatus.ACTIVE);
        }
        return workspaceTagRepository.findAllByWorkspaceIdAndTagStatusAndCreatedByIdOrderByNameAsc(workspaceId, TagStatus.ACTIVE, userId);
    }

    @Override
    @Transactional
    public WorkspaceTag saveTag(WorkspaceTag workspaceTag) {
        return workspaceTagRepository.save(workspaceTag);
    }

    @Override
    @Transactional
    public WorkspaceTag saveTag(WorkspaceTag workspaceTag, UUID userId) {
        UUID workspaceId = workspaceTag.getWorkspace() == null ? null : workspaceTag.getWorkspace().getId();
        boolean hasRelation = canAccessWorkspace(workspaceId, userId) || hasAccessiblePageInWorkspace(workspaceId, userId);
        boolean isCreate = workspaceTag.getId() == null;

        if (isCreate) {
            if (!hasRelation) {
                throw new EntityNotFoundException("Workspace not found or access denied: " + workspaceId);
            }
            workspaceTag.setCreatedBy(requireUser(userId));
            if (workspaceTag.getCreatedAt() == null) {
                workspaceTag.setCreatedAt(OffsetDateTime.now());
            }
            return workspaceTagRepository.save(workspaceTag);
        }

        WorkspaceTag existing = workspaceTagRepository.findById(workspaceTag.getId())
            .orElseThrow(() -> new EntityNotFoundException("Tag not found: " + workspaceTag.getId()));
        boolean createdByUser = existing.getCreatedBy() != null
            && existing.getCreatedBy().getId() != null
            && existing.getCreatedBy().getId().equals(userId);

        if (!createdByUser && !hasRelation) {
            throw new EntityNotFoundException("Tag not found or access denied: " + workspaceTag.getId());
        }

        existing.setWorkspace(workspaceTag.getWorkspace());
        existing.setName(workspaceTag.getName());
        existing.setTagStatus(workspaceTag.getTagStatus());
        return workspaceTagRepository.save(existing);
    }

    @Override
    @Transactional
    public PageTagAssignment assignTag( UUID pageId, UUID workspaceId, UUID tagId, UUID actorUserId) {
        return doAssignTag(pageId, workspaceId, tagId, actorUserId);
    }

    private PageTagAssignment doAssignTag(UUID pageId, UUID workspaceId, UUID tagId, UUID actorUserId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new EntityNotFoundException("Workspace not found: " + workspaceId));
        WikiPage page = wikiPageRepository.findById(pageId)
            .orElseThrow(() -> new EntityNotFoundException("Wiki page not found: " + pageId));
        WorkspaceTag tag = workspaceTagRepository.findById(tagId)
            .orElseThrow(() -> new EntityNotFoundException("Tag not found: " + tagId));
        if (!workspace.getId().equals(tag.getWorkspace().getId())) {
            throw new IllegalArgumentException("Tag does not belong to workspace: " + workspaceId);
        }
        if (!pageWorkspaceLinkRepository.existsByPageIdAndWorkspaceId(pageId, workspaceId)) {
            throw new EntityNotFoundException("Page is not linked to workspace. pageId=" + pageId + ", workspaceId=" + workspaceId);
        }

        return pageTagAssignmentRepository.findByPageIdAndWorkspaceIdAndTagId(pageId, workspaceId, tagId)
            .orElseGet(() -> {
                AppUser actor = actorUserId == null ? null : appUserRepository.findById(actorUserId)
                                                                              .orElseThrow(() -> new EntityNotFoundException("User not found: " + actorUserId));
                PageTagAssignment assignment = new PageTagAssignment();
                assignment.setPage(page);
                assignment.setWorkspace(workspace);
                assignment.setTag(tag);
                assignment.setAssignmentStatus( AssignmentStatus.ACTIVE);
                assignment.setCreatedBy(actor);
                assignment.setCreatedAt(OffsetDateTime.now());
                return pageTagAssignmentRepository.save(assignment);
            });
    }

    @Override
    @Transactional
    public PageTagAssignment assignTag(UUID pageId, UUID workspaceId, UUID tagId, UUID actorUserId, UUID userId) {
        if (!canAccessWorkspace(workspaceId, userId) && !wikiPageRepository.existsAccessibleByIdAndUserId(pageId, userId)) {
            throw new EntityNotFoundException("Workspace/page not found or access denied. workspaceId=" + workspaceId + ", pageId=" + pageId);
        }
        return doAssignTag(pageId, workspaceId, tagId, actorUserId);
    }

    @Override
    public List<PageTagAssignment> getPageAssignments(UUID pageId, UUID workspaceId) {
        return pageTagAssignmentRepository.findAllByPageIdAndWorkspaceIdAndAssignmentStatus(pageId, workspaceId, AssignmentStatus.ACTIVE);
    }

    @Override
    public List<PageTagAssignment> getPageAssignments(UUID pageId, UUID workspaceId, UUID userId) {
        if (canAccessWorkspace(workspaceId, userId) || wikiPageRepository.existsAccessibleByIdAndUserId(pageId, userId)) {
            return getPageAssignments(pageId, workspaceId);
        }
        return pageTagAssignmentRepository.findAllByPageIdAndWorkspaceIdAndAssignmentStatusAndCreatedById(
            pageId,
            workspaceId,
            AssignmentStatus.ACTIVE,
            userId
        );
    }

    private AppUser requireUser(UUID userId) {
        return appUserRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    private boolean canAccessWorkspace(UUID workspaceId, UUID userId) {
        if (workspaceId == null || userId == null) {
            return false;
        }
        return workspaceRepository.findById(workspaceId)
            .map(workspace -> (workspace.getCreatedBy() != null
                && workspace.getCreatedBy().getId() != null
                && workspace.getCreatedBy().getId().equals(userId))
                || workspaceMembershipRepository.existsByWorkspaceIdAndUserIdAndStatus(workspaceId, userId, MembershipStatus.ACTIVE))
            .orElse(false);
    }

    private boolean hasAccessiblePageInWorkspace(UUID workspaceId, UUID userId) {
        if (workspaceId == null || userId == null) {
            return false;
        }
        return wikiPageRepository.existsAccessibleByWorkspaceIdAndUserId(workspaceId, userId);
    }
}
