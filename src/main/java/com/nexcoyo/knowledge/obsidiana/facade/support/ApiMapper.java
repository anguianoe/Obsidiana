package com.nexcoyo.knowledge.obsidiana.facade.support;


import com.nexcoyo.knowledge.obsidiana.dto.response.AssetUsageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.AuditEventResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.CommentThreadResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageAssetResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageCommentReactionResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageCommentResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageLinkResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageTagAssignmentResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageTreeNodeResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PublicPagePublicationResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PublicPageSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.RestoreAuditResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.RevisionSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.StoredAssetResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.TrashRecordResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WikiPageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WikiPageRevisionResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceInvitationResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceMembershipResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WorkspaceTagResponse;
import com.nexcoyo.knowledge.obsidiana.entity.AuditEvent;
import com.nexcoyo.knowledge.obsidiana.entity.BaseUuidEntity;
import com.nexcoyo.knowledge.obsidiana.entity.PageAsset;
import com.nexcoyo.knowledge.obsidiana.entity.PageComment;
import com.nexcoyo.knowledge.obsidiana.entity.PageCommentReaction;
import com.nexcoyo.knowledge.obsidiana.entity.PageTagAssignment;
import com.nexcoyo.knowledge.obsidiana.entity.PageWorkspaceLink;
import com.nexcoyo.knowledge.obsidiana.entity.PublicPagePublication;
import com.nexcoyo.knowledge.obsidiana.entity.RestoreAudit;
import com.nexcoyo.knowledge.obsidiana.entity.StoredAsset;
import com.nexcoyo.knowledge.obsidiana.entity.TrashRecord;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPageRevision;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceInvitation;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceTag;
import com.nexcoyo.knowledge.obsidiana.projection.AssetUsageProjection;
import com.nexcoyo.knowledge.obsidiana.projection.CommentThreadProjection;
import com.nexcoyo.knowledge.obsidiana.projection.PageTreeNodeProjection;
import com.nexcoyo.knowledge.obsidiana.projection.PublicPageSummaryProjection;
import com.nexcoyo.knowledge.obsidiana.projection.RevisionSummaryProjection;
import com.nexcoyo.knowledge.obsidiana.projection.WorkspaceSummaryProjection;

import java.util.Optional;

public final class ApiMapper {
    private ApiMapper() {}

    public static WorkspaceResponse toResponse( Workspace entity) {
        return new WorkspaceResponse(
            entity.getId(),
            entity.getName(),
            entity.getSlug(),
            entity.getKind(),
            entity.getStatus(),
            entity.getApprovalStatus(),
            idOf(entity.getCreatedBy()),
            idOf(entity.getApprovedBy()),
            entity.getApprovedAt(),
            entity.getDescription(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public static WorkspaceSummaryResponse toResponse( WorkspaceSummaryProjection p) {
        return new WorkspaceSummaryResponse(
            p.getWorkspaceId(), p.getWorkspaceName(), p.getWorkspaceSlug(),
            p.getKind(), p.getStatus(), p.getApprovalStatus(), p.getMembershipRole(), p.getJoinedAt()
        );
    }

    public static WorkspaceMembershipResponse toResponse( WorkspaceMembership entity) {
        return new WorkspaceMembershipResponse(
            entity.getId(),
            idOf(entity.getWorkspace()),
            idOf(entity.getUser()),
            entity.getRole(),
            entity.getStatus(),
            entity.getJoinedAt(),
            entity.getInvitedAt(),
            idOf(entity.getCreatedBy()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public static WorkspaceInvitationResponse toResponse( WorkspaceInvitation entity) {
        return new WorkspaceInvitationResponse(
            entity.getId(),
            idOf(entity.getWorkspace()),
            entity.getInvitedEmail(),
            idOf(entity.getInvitedUser()),
            entity.getRole(),
            entity.getStatus(),
            idOf(entity.getInvitedBy()),
            entity.getExpiresAt(),
            entity.getAcceptedAt(),
            entity.getRejectedAt(),
            entity.getRevokedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public static WikiPageResponse toResponse( WikiPage entity) {
        return new WikiPageResponse(
            entity.getId(),
            entity.getPublicUuid(),
            idOf(entity.getOwnerUser()),
            entity.getTitle(),
            entity.getSlug(),
            entity.getEditMode(),
            entity.getPageStatus(),
            entity.getIsEncrypted(),
            entity.getIsPublicable(),
            idOf(entity.getCurrentRevision()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public static PageLinkResponse toResponse( PageWorkspaceLink entity) {
        return new PageLinkResponse(entity.getId(), idOf(entity.getPage()), idOf(entity.getWorkspace()), entity.getLinkedAt(), idOf(entity.getLinkedBy()));
    }

    public static PageTreeNodeResponse toResponse( PageTreeNodeProjection p) {
        return new PageTreeNodeResponse(p.getPageId(), p.getTitle(), p.getSlug(), p.getSortOrder(), p.getChildCount());
    }

    public static WikiPageRevisionResponse toResponse( WikiPageRevision entity) {
        return new WikiPageRevisionResponse(
            entity.getId(), idOf(entity.getPage()), entity.getRevisionNumber(), entity.getTitleSnapshot(), entity.getEditorType(),
            entity.getContentHtml(), entity.getContentText(), entity.getChangeSummary(), entity.getIsEncrypted(),
            entity.getContentIv(), entity.getContentAuthTag(), entity.getEncryptionKdf(), entity.getIsPinned(),
            idOf(entity.getCreatedBy()), entity.getCreatedAt()
        );
    }

    public static RevisionSummaryResponse toResponse( RevisionSummaryProjection p) {
        return new RevisionSummaryResponse(p.getRevisionId(), p.getRevisionNumber(), p.getTitleSnapshot(), p.getPinned(), p.getCreatedBy(), p.getCreatedAt());
    }

    public static PublicPagePublicationResponse toResponse( PublicPagePublication entity) {
        return new PublicPagePublicationResponse(
            entity.getId(), idOf(entity.getPage()), idOf(entity.getRevision()), entity.getPublicSlug(), entity.getPublicTitle(),
            entity.getPublicHtml(), entity.getPublicationStatus(), idOf(entity.getPublishedBy()), entity.getPublishedAt(), entity.getUnpublishedAt()
        );
    }

    public static PublicPageSummaryResponse toResponse( PublicPageSummaryProjection p) {
        return new PublicPageSummaryResponse(p.getPublicationId(), p.getPageId(), p.getRevisionId(), p.getPublicSlug(), p.getPublicTitle(), p.getPublishedAt());
    }

    public static StoredAssetResponse toResponse( StoredAsset entity) {
        return new StoredAssetResponse(
            entity.getId(), entity.getStorageProvider(), entity.getBucketName(), entity.getObjectKey(), entity.getOriginalFilename(),
            entity.getNormalizedFilename(), entity.getMimeType(), entity.getAssetType(), entity.getFileExtension(), entity.getSizeBytes(),
            entity.getChecksumSha256(), entity.getStatus(), idOf(entity.getUploadedBy()), entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }

    public static PageAssetResponse toResponse( PageAsset entity) {
        return new PageAssetResponse(
            entity.getId(), idOf(entity.getPage()), idOf(entity.getAsset()), entity.getAssetRole(), entity.getDisplayName(),
            entity.getSortOrder(), idOf(entity.getCreatedBy()), entity.getCreatedAt()
        );
    }

    public static AssetUsageResponse toResponse( AssetUsageProjection p) {
        return new AssetUsageResponse(p.getAssetId(), p.getPageLinks(), p.getRevisionRefs(), p.getPublicLinks());
    }

    public static PageCommentResponse toResponse( PageComment entity) {
        return new PageCommentResponse(
            entity.getId(), idOf(entity.getPage()), idOf(entity.getWorkspace()), idOf(entity.getAuthorUser()), idOf(entity.getParentComment()),
            entity.getBody(), entity.getCommentStatus(), entity.getCreatedAt(), entity.getEditedAt(), entity.getDeletedAt()
        );
    }

    public static CommentThreadResponse toResponse( CommentThreadProjection p) {
        return new CommentThreadResponse(
            p.getCommentId(), p.getParentCommentId(), p.getAuthorUserId(), p.getBody(), p.getCreatedAt(), p.getReactionCount(), p.getReplyCount()
        );
    }

    public static PageCommentReactionResponse toResponse( PageCommentReaction entity) {
        return new PageCommentReactionResponse(entity.getId(), idOf(entity.getComment()), idOf(entity.getUser()), entity.getReactionType(), entity.getCreatedAt());
    }

    public static WorkspaceTagResponse toResponse( WorkspaceTag entity) {
        return new WorkspaceTagResponse(entity.getId(), idOf(entity.getWorkspace()), entity.getName(), entity.getTagStatus(), idOf(entity.getCreatedBy()), entity.getCreatedAt());
    }

    public static PageTagAssignmentResponse toResponse( PageTagAssignment entity) {
        return new PageTagAssignmentResponse(entity.getId(), idOf(entity.getPage()), idOf(entity.getWorkspace()), idOf(entity.getTag()), entity.getAssignmentStatus(), idOf(entity.getCreatedBy()), entity.getCreatedAt());
    }

    public static TrashRecordResponse toResponse( TrashRecord entity) {
        return new TrashRecordResponse(
            entity.getId(), entity.getEntityType(), entity.getEntityId(), idOf(entity.getWorkspace()), idOf(entity.getPage()), idOf(entity.getAsset()),
            idOf(entity.getComment()), idOf(entity.getDeletedBy()), entity.getDeleteReason(), entity.getSnapshotPayload(), entity.getDeletedAt(),
            entity.getRestoreDeadlineAt(), entity.getRestoredAt(), entity.getPurgeScheduledAt(), entity.getStatus()
        );
    }

    public static RestoreAuditResponse toResponse( RestoreAudit entity) {
        return new RestoreAuditResponse(
            entity.getId(), idOf(entity.getTrashRecord()), entity.getEntityType(), entity.getEntityId(), idOf(entity.getRestoredBy()),
            entity.getRestoreReason(), entity.getRestorePayload(), entity.getRestoredAt()
        );
    }

    public static AuditEventResponse toResponse( AuditEvent entity) {
        return new AuditEventResponse(
            entity.getId(), entity.getEventType(), entity.getEntityType(), entity.getEntityId(),
            idOf(entity.getActorUser()), idOf(entity.getWorkspace()), entity.getEventPayload(), entity.getCreatedAt()
        );
    }

    private static java.util.UUID idOf(Object entity) {
        if (entity == null) return null;
        if (entity instanceof BaseUuidEntity e) return e.getId();
        return null;
    }
}
