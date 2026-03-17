package com.nexcoyo.knowledge.obsidiana.repository;


import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceInvitation;
import com.nexcoyo.knowledge.obsidiana.util.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, UUID>, JpaSpecificationExecutor<WorkspaceInvitation> {
    List< WorkspaceInvitation > findAllByWorkspaceIdAndStatus( UUID workspaceId, InvitationStatus status);
    List<WorkspaceInvitation> findAllByInvitedEmailIgnoreCaseAndStatus(String invitedEmail, InvitationStatus status);
    Optional<WorkspaceInvitation> findByInvitationTokenHash(String invitationTokenHash);
    List<WorkspaceInvitation> findAllByStatusAndExpiresAtBefore( InvitationStatus status, OffsetDateTime expiresAt);
}
