package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.PageTagAssignment;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceTag;
import com.nexcoyo.knowledge.obsidiana.repository.AppUserRepository;
import com.nexcoyo.knowledge.obsidiana.repository.PageTagAssignmentRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WikiPageRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceTagRepository;
import com.nexcoyo.knowledge.obsidiana.service.TagService;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssignmentStatus;
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
    private final AppUserRepository appUserRepository;

    @Override
    public List< WorkspaceTag > getActiveTags( UUID workspaceId) {
        return workspaceTagRepository.findAllByWorkspaceIdAndTagStatusOrderByNameAsc(workspaceId, TagStatus.ACTIVE);
    }

    @Override
    @Transactional
    public WorkspaceTag saveTag(WorkspaceTag workspaceTag) {
        return workspaceTagRepository.save(workspaceTag);
    }

    @Override
    @Transactional
    public PageTagAssignment assignTag( UUID pageId, UUID workspaceId, UUID tagId, UUID actorUserId) {
        return pageTagAssignmentRepository.findByPageIdAndWorkspaceIdAndTagId(pageId, workspaceId, tagId)
            .orElseGet(() -> {
                WikiPage page = wikiPageRepository.findById(pageId)
                                                  .orElseThrow(() -> new EntityNotFoundException("Wiki page not found: " + pageId));
                Workspace workspace = workspaceRepository.findById(workspaceId)
                                                         .orElseThrow(() -> new EntityNotFoundException("Workspace not found: " + workspaceId));
                WorkspaceTag tag = workspaceTagRepository.findById(tagId)
                    .orElseThrow(() -> new EntityNotFoundException("Tag not found: " + tagId));
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
    public List<PageTagAssignment> getPageAssignments(UUID pageId, UUID workspaceId) {
        return pageTagAssignmentRepository.findAllByPageIdAndWorkspaceIdAndAssignmentStatus(pageId, workspaceId, AssignmentStatus.ACTIVE);
    }
}
