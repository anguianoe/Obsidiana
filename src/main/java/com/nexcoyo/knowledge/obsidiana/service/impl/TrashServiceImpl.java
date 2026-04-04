package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.PageComment;
import com.nexcoyo.knowledge.obsidiana.entity.RestoreAudit;
import com.nexcoyo.knowledge.obsidiana.entity.StoredAsset;
import com.nexcoyo.knowledge.obsidiana.entity.TrashRecord;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.repository.AppUserRepository;
import com.nexcoyo.knowledge.obsidiana.repository.PageCommentRepository;
import com.nexcoyo.knowledge.obsidiana.repository.RestoreAuditRepository;
import com.nexcoyo.knowledge.obsidiana.repository.StoredAssetRepository;
import com.nexcoyo.knowledge.obsidiana.repository.TrashRecordRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WikiPageRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceMembershipRepository;
import com.nexcoyo.knowledge.obsidiana.repository.WorkspaceRepository;
import com.nexcoyo.knowledge.obsidiana.service.TrashService;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.TrashRecordSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.service.specification.TrashRecordSpecifications;
import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashStatus;
import jakarta.persistence.EntityNotFoundException;

import java.time.Instant;
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
public class TrashServiceImpl implements TrashService {

    private final TrashRecordRepository trashRecordRepository;
    private final RestoreAuditRepository restoreAuditRepository;
    private final AppUserRepository appUserRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMembershipRepository workspaceMembershipRepository;
    private final WikiPageRepository wikiPageRepository;
    private final PageCommentRepository pageCommentRepository;
    private final StoredAssetRepository storedAssetRepository;

    @Override
    public Page< TrashRecord > search( TrashRecordSearchCriteria criteria, Pageable pageable) {
        return trashRecordRepository.findAll(TrashRecordSpecifications.byCriteria(criteria), pageable);
    }

    @Override
    @Transactional
    public TrashRecord moveToTrash(TrashRecord trashRecord) {
        if (trashRecord.getDeletedAt() == null) {
            trashRecord.setDeletedAt( Instant.now());
        }
        return trashRecordRepository.save(trashRecord);
    }

    @Override
    public TrashRecord getRequired(UUID trashRecordId) {
        return trashRecordRepository.findById(trashRecordId)
            .orElseThrow(() -> new EntityNotFoundException("Trash record not found: " + trashRecordId));
    }

    @Override
    @Transactional
    public RestoreAudit restore( UUID trashRecordId, UUID restoredBy, String reason) {
        TrashRecord trashRecord = getRequired(trashRecordId);
        AppUser actor = appUserRepository.findById(restoredBy)
                                         .orElseThrow(() -> new EntityNotFoundException("User not found: " + restoredBy));

        trashRecord.setStatus( TrashStatus.RESTORED);
        trashRecord.setRestoredAt(Instant.now());
        trashRecordRepository.save(trashRecord);

        RestoreAudit audit = new RestoreAudit();
        audit.setTrashRecord(trashRecord);
        audit.setEntityType(trashRecord.getEntityType());
        audit.setEntityId(trashRecord.getEntityId());
        audit.setRestoredBy(actor);
        audit.setRestoreReason(reason);
        audit.setRestoredAt(Instant.now());
        return restoreAuditRepository.save(audit);
    }

    @Override
    public List<TrashRecord> findOverdueTrash() {
        return trashRecordRepository.findAllByStatusAndRestoreDeadlineAtBefore(TrashStatus.TRASHED, Instant.now());
    }


    @Override
    public Page< TrashRecord > search( TrashRecordSearchCriteria criteria, Pageable pageable, UUID userId) {
        return trashRecordRepository.findAll(TrashRecordSpecifications.visibleToUser(criteria, userId), pageable);
    }

    @Override
    @Transactional
    public TrashRecord moveToTrash(TrashRecord trashRecord, UUID userId) {
        enforceMoveToTrashAccess(trashRecord, userId);
        if (trashRecord.getDeletedAt() == null) {
            trashRecord.setDeletedAt( Instant.now());
        }
        trashRecord.setDeletedBy(requireUser(userId));
        return trashRecordRepository.save(trashRecord);
    }

    @Override
    public TrashRecord getRequired(UUID trashRecordId, UUID userId) {
        return trashRecordRepository.findByIdAndDeletedById(trashRecordId, userId)
                                     .orElseThrow(() -> new EntityNotFoundException("Trash record not found: " + trashRecordId));
    }

    @Override
    @Transactional
    public RestoreAudit restore( UUID trashRecordId, UUID restoredBy, String reason, UUID userId) {
        TrashRecord trashRecord = getRequired(trashRecordId);
        if (!canRestoreTrashRecord(trashRecord, userId)) {
            throw new EntityNotFoundException("Trash record not found: " + trashRecordId);
        }
        AppUser actor = requireUser(userId);

        trashRecord.setStatus( TrashStatus.RESTORED);
        trashRecord.setRestoredAt(Instant.now());
        trashRecordRepository.save(trashRecord);

        RestoreAudit audit = new RestoreAudit();
        audit.setTrashRecord(trashRecord);
        audit.setEntityType(trashRecord.getEntityType());
        audit.setEntityId(trashRecord.getEntityId());
        audit.setRestoredBy(actor);
        audit.setRestoreReason(reason);
        audit.setRestoredAt(Instant.now());
        return restoreAuditRepository.save(audit);
    }

    @Override
    public List<TrashRecord> findOverdueTrash(UUID userId) {
        return trashRecordRepository.findAllByStatusAndRestoreDeadlineAtBeforeAndDeletedById(TrashStatus.TRASHED, Instant.now(), userId);
    }

    private AppUser requireUser(UUID userId) {
        return appUserRepository.findById(userId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    private void enforceMoveToTrashAccess(TrashRecord trashRecord, UUID userId) {
        boolean allowed = switch (trashRecord.getEntityType()) {
            case WORKSPACE -> canAccessWorkspace(resolveWorkspaceForMove(trashRecord).getId(), userId);
            case PAGE -> canAccessPage(resolvePageForMove(trashRecord).getId(), userId);
            case COMMENT -> canAccessComment(resolveCommentForMove(trashRecord), userId);
            case ASSET -> canAccessAsset(resolveAssetForMove(trashRecord), userId)
                || canAccessWorkspace(idOf(trashRecord.getWorkspace()), userId)
                || canAccessPage(idOf(trashRecord.getPage()), userId)
                || canAccessComment(trashRecord.getComment(), userId);
        };

        if (!allowed) {
            throw new EntityNotFoundException("Trash target not found or access denied: " + trashRecord.getEntityId());
        }
    }

    private boolean canRestoreTrashRecord(TrashRecord trashRecord, UUID userId) {
        return isDeletedByUser(trashRecord, userId)
            || canAccessWorkspace(idOf(trashRecord.getWorkspace()), userId)
            || canAccessPage(idOf(trashRecord.getPage()), userId)
            || canAccessComment(trashRecord.getComment(), userId)
            || canAccessAsset(trashRecord.getAsset(), userId)
            || switch (trashRecord.getEntityType()) {
                case WORKSPACE -> canAccessWorkspace(trashRecord.getEntityId(), userId);
                case PAGE -> canAccessPage(trashRecord.getEntityId(), userId);
                case COMMENT -> canAccessComment(loadComment(trashRecord.getEntityId()), userId);
                case ASSET -> canAccessAsset(loadAsset(trashRecord.getEntityId()), userId);
            };
    }

    private Workspace resolveWorkspaceForMove(TrashRecord trashRecord) {
        Workspace workspace = workspaceRepository.findById(trashRecord.getEntityId())
            .orElseThrow(() -> new EntityNotFoundException("Workspace not found: " + trashRecord.getEntityId()));
        trashRecord.setWorkspace(workspace);
        return workspace;
    }

    private WikiPage resolvePageForMove(TrashRecord trashRecord) {
        WikiPage page = wikiPageRepository.findById(trashRecord.getEntityId())
            .orElseThrow(() -> new EntityNotFoundException("Wiki page not found: " + trashRecord.getEntityId()));
        trashRecord.setPage(page);
        return page;
    }

    private PageComment resolveCommentForMove(TrashRecord trashRecord) {
        PageComment comment = loadComment(trashRecord.getEntityId());
        trashRecord.setComment(comment);
        if (trashRecord.getWorkspace() == null) {
            trashRecord.setWorkspace(comment.getWorkspace());
        }
        if (trashRecord.getPage() == null) {
            trashRecord.setPage(comment.getPage());
        }
        return comment;
    }

    private StoredAsset resolveAssetForMove(TrashRecord trashRecord) {
        StoredAsset asset = loadAsset(trashRecord.getEntityId());
        trashRecord.setAsset(asset);
        return asset;
    }

    private PageComment loadComment(UUID commentId) {
        if (commentId == null) {
            return null;
        }
        return pageCommentRepository.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException("Comment not found: " + commentId));
    }

    private StoredAsset loadAsset(UUID assetId) {
        if (assetId == null) {
            return null;
        }
        return storedAssetRepository.findById(assetId)
            .orElseThrow(() -> new EntityNotFoundException("Asset not found: " + assetId));
    }

    private boolean isDeletedByUser(TrashRecord trashRecord, UUID userId) {
        return trashRecord.getDeletedBy() != null
            && trashRecord.getDeletedBy().getId() != null
            && trashRecord.getDeletedBy().getId().equals(userId);
    }

    private boolean canAccessWorkspace(UUID workspaceId, UUID userId) {
        if (workspaceId == null || userId == null) {
            return false;
        }
        return workspaceRepository.findById(workspaceId)
            .map(workspace -> isWorkspaceCreator(workspace, userId)
                || workspaceMembershipRepository.existsByWorkspaceIdAndUserIdAndStatus(workspaceId, userId, MembershipStatus.ACTIVE))
            .orElse(false);
    }

    private boolean isWorkspaceCreator(Workspace workspace, UUID userId) {
        return workspace.getCreatedBy() != null
            && workspace.getCreatedBy().getId() != null
            && workspace.getCreatedBy().getId().equals(userId);
    }

    private boolean canAccessPage(UUID pageId, UUID userId) {
        return pageId != null && userId != null && wikiPageRepository.existsAccessibleByIdAndUserId(pageId, userId);
    }

    private boolean canAccessComment(PageComment comment, UUID userId) {
        if (comment == null || userId == null) {
            return false;
        }
        return (comment.getAuthorUser() != null && userId.equals(comment.getAuthorUser().getId()))
            || canAccessWorkspace(idOf(comment.getWorkspace()), userId)
            || canAccessPage(idOf(comment.getPage()), userId);
    }

    private boolean canAccessAsset(StoredAsset asset, UUID userId) {
        return asset != null
            && asset.getUploadedBy() != null
            && userId != null
            && userId.equals(asset.getUploadedBy().getId());
    }

    private UUID idOf(Workspace workspace) {
        return workspace == null ? null : workspace.getId();
    }

    private UUID idOf(WikiPage page) {
        return page == null ? null : page.getId();
    }
}
