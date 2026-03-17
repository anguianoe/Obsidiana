package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceInvitation;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.projection.WorkspaceSummaryProjection;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceInvitationRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceMembershipRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceRepository;
import com.nexcoyo.knowledge.obsidiana.service.WorkspaceService;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.WorkspaceSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.service.specification.WorkspaceSpecifications;
import com.nexcoyo.knowledge.obsidiana.util.enums.InvitationStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMembershipRepository workspaceMembershipRepository;
    private final WorkspaceInvitationRepository workspaceInvitationRepository;

    @Override
    public Page< Workspace > search( WorkspaceSearchCriteria criteria, Pageable pageable) {
        return workspaceRepository.findAll(WorkspaceSpecifications.byCriteria(criteria), pageable);
    }

    @Override
    public List< WorkspaceSummaryProjection > findAccessibleSummaries( UUID userId) {
        return workspaceRepository.findAccessibleWorkspaceSummaries(userId);
    }

    @Override
    public Workspace getRequired(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new EntityNotFoundException("Workspace not found: " + workspaceId));
    }

    @Override
    @Transactional
    public Workspace save(Workspace workspace) {
        return workspaceRepository.save(workspace);
    }

    @Override
    public List< WorkspaceMembership > getActiveMembers( UUID workspaceId) {
        return workspaceMembershipRepository.findAllByWorkspaceIdAndStatus(workspaceId, MembershipStatus.ACTIVE);
    }

    @Override
    public List< WorkspaceInvitation > getPendingInvitations( UUID workspaceId) {
        return workspaceInvitationRepository.findAllByWorkspaceIdAndStatus(workspaceId, InvitationStatus.PENDING);
    }
}
