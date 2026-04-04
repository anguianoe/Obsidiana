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
import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.SystemRole;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashEntityType;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.UserStatus;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrashServiceImplTest {

    @Mock private TrashRecordRepository trashRecordRepository;
    @Mock private RestoreAuditRepository restoreAuditRepository;
    @Mock private AppUserRepository appUserRepository;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private WorkspaceMembershipRepository workspaceMembershipRepository;
    @Mock private WikiPageRepository wikiPageRepository;
    @Mock private PageCommentRepository pageCommentRepository;
    @Mock private StoredAssetRepository storedAssetRepository;

    private TrashServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TrashServiceImpl(
            trashRecordRepository,
            restoreAuditRepository,
            appUserRepository,
            workspaceRepository,
            workspaceMembershipRepository,
            wikiPageRepository,
            pageCommentRepository,
            storedAssetRepository
        );
    }

    @Test
    void getRequiredForUserReturnsOnlyRecordsDeletedBySessionUser() {
        UUID trashRecordId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TrashRecord trashRecord = trashRecord(trashRecordId, TrashEntityType.PAGE, UUID.randomUUID());

        when(trashRecordRepository.findByIdAndDeletedById(trashRecordId, userId)).thenReturn(Optional.of(trashRecord));

        TrashRecord result = service.getRequired(trashRecordId, userId);

        assertThat(result).isSameAs(trashRecord);
    }

    @Test
    void getRequiredForUserRejectsRecordsDeletedBySomeoneElse() {
        UUID trashRecordId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(trashRecordRepository.findByIdAndDeletedById(trashRecordId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRequired(trashRecordId, userId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Trash record not found");
    }

    @Test
    void moveToTrashAllowsAccessiblePageAndOverridesDeletedByWithSessionUser() {
        UUID userId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        AppUser actor = user(userId);
        WikiPage page = new WikiPage();
        page.setId(pageId);

        TrashRecord trashRecord = trashRecord(null, TrashEntityType.PAGE, pageId);
        trashRecord.setDeletedBy(user(UUID.randomUUID()));

        when(wikiPageRepository.findById(pageId)).thenReturn(Optional.of(page));
        when(wikiPageRepository.existsAccessibleByIdAndUserId(pageId, userId)).thenReturn(true);
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(actor));
        when(trashRecordRepository.save(any(TrashRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TrashRecord saved = service.moveToTrash(trashRecord, userId);

        assertThat(saved.getPage()).isSameAs(page);
        assertThat(saved.getDeletedBy()).isSameAs(actor);
        assertThat(saved.getDeletedAt()).isNotNull();
    }

    @Test
    void moveToTrashRejectsUnauthorizedWorkspaceTrash() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        Workspace workspace = workspace(workspaceId, UUID.randomUUID());
        TrashRecord trashRecord = trashRecord(null, TrashEntityType.WORKSPACE, workspaceId);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceMembershipRepository.existsByWorkspaceIdAndUserIdAndStatus(workspaceId, userId, MembershipStatus.ACTIVE))
            .thenReturn(false);

        assertThatThrownBy(() -> service.moveToTrash(trashRecord, userId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("access denied");

        verify(trashRecordRepository, never()).save(any(TrashRecord.class));
    }

    @Test
    void moveToTrashAllowsAssetUploader() {
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        AppUser actor = user(userId);
        StoredAsset asset = new StoredAsset();
        asset.setId(assetId);
        asset.setUploadedBy(actor);

        TrashRecord trashRecord = trashRecord(null, TrashEntityType.ASSET, assetId);

        when(storedAssetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(actor));
        when(trashRecordRepository.save(any(TrashRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TrashRecord saved = service.moveToTrash(trashRecord, userId);

        assertThat(saved.getAsset()).isSameAs(asset);
        assertThat(saved.getDeletedBy()).isSameAs(actor);
    }

    @Test
    void restoreAllowsDeletedByUserAndUsesSessionUserAsAuditActor() {
        UUID trashRecordId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AppUser actor = user(userId);
        TrashRecord trashRecord = trashRecord(trashRecordId, TrashEntityType.PAGE, UUID.randomUUID());
        trashRecord.setDeletedBy(actor);

        when(trashRecordRepository.findById(trashRecordId)).thenReturn(Optional.of(trashRecord));
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(actor));
        when(restoreAuditRepository.save(any(RestoreAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RestoreAudit audit = service.restore(trashRecordId, UUID.randomUUID(), "restore", userId);

        assertThat(trashRecord.getStatus()).isEqualTo(TrashStatus.RESTORED);
        assertThat(trashRecord.getRestoredAt()).isNotNull();
        assertThat(audit.getRestoredBy()).isSameAs(actor);
        assertThat(audit.getRestoreReason()).isEqualTo("restore");
        verify(trashRecordRepository).save(trashRecord);
    }

    @Test
    void restoreAllowsCommentAuthor() {
        UUID trashRecordId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        AppUser actor = user(userId);
        PageComment comment = new PageComment();
        comment.setId(commentId);
        comment.setAuthorUser(actor);

        TrashRecord trashRecord = trashRecord(trashRecordId, TrashEntityType.COMMENT, commentId);
        trashRecord.setComment(comment);

        when(trashRecordRepository.findById(trashRecordId)).thenReturn(Optional.of(trashRecord));
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(actor));
        when(restoreAuditRepository.save(any(RestoreAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RestoreAudit audit = service.restore(trashRecordId, UUID.randomUUID(), "reason", userId);

        assertThat(audit.getRestoredBy()).isSameAs(actor);
        assertThat(trashRecord.getStatus()).isEqualTo(TrashStatus.RESTORED);
    }

    @Test
    void restoreAllowsWorkspaceMembershipOnLinkedWorkspace() {
        UUID trashRecordId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        AppUser actor = user(userId);
        Workspace workspace = workspace(workspaceId, UUID.randomUUID());

        TrashRecord trashRecord = trashRecord(trashRecordId, TrashEntityType.WORKSPACE, workspaceId);
        trashRecord.setWorkspace(workspace);

        when(trashRecordRepository.findById(trashRecordId)).thenReturn(Optional.of(trashRecord));
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceMembershipRepository.existsByWorkspaceIdAndUserIdAndStatus(workspaceId, userId, MembershipStatus.ACTIVE))
            .thenReturn(true);
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(actor));
        when(restoreAuditRepository.save(any(RestoreAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RestoreAudit audit = service.restore(trashRecordId, UUID.randomUUID(), "reason", userId);

        assertThat(audit.getRestoredBy()).isSameAs(actor);
        assertThat(trashRecord.getStatus()).isEqualTo(TrashStatus.RESTORED);
    }

    @Test
    void restoreRejectsUnauthorizedAssetRestore() {
        UUID trashRecordId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID ignoredRestoredBy = UUID.randomUUID();
        AppUser uploader = user(UUID.randomUUID());
        StoredAsset asset = new StoredAsset();
        asset.setId(assetId);
        asset.setUploadedBy(uploader);

        TrashRecord trashRecord = trashRecord(trashRecordId, TrashEntityType.ASSET, assetId);

        when(trashRecordRepository.findById(trashRecordId)).thenReturn(Optional.of(trashRecord));
        when(storedAssetRepository.findById(assetId)).thenReturn(Optional.of(asset));

        assertThatThrownBy(() -> service.restore(trashRecordId, ignoredRestoredBy, "reason", userId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Trash record not found");
    }

    @Test
    void findOverdueTrashForUserIsScopedByDeletedBy() {
        UUID userId = UUID.randomUUID();
        List<TrashRecord> expected = List.of(trashRecord(UUID.randomUUID(), TrashEntityType.PAGE, UUID.randomUUID()));

        when(trashRecordRepository.findAllByStatusAndRestoreDeadlineAtBeforeAndDeletedById(eq(TrashStatus.TRASHED), any(Instant.class), eq(userId)))
            .thenReturn(expected);

        List<TrashRecord> result = service.findOverdueTrash(userId);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void searchForUserBuildsScopedSpecification() {
        service.search(new com.nexcoyo.knowledge.obsidiana.service.dto.search.TrashRecordSearchCriteria(), org.springframework.data.domain.Pageable.unpaged(), UUID.randomUUID());

        verify(trashRecordRepository).findAll(
            org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<TrashRecord>>any(),
            eq(org.springframework.data.domain.Pageable.unpaged())
        );
    }

    private static TrashRecord trashRecord(UUID id, TrashEntityType entityType, UUID entityId) {
        TrashRecord trashRecord = new TrashRecord();
        trashRecord.setId(id);
        trashRecord.setEntityType(entityType);
        trashRecord.setEntityId(entityId);
        trashRecord.setStatus(TrashStatus.TRASHED);
        return trashRecord;
    }

    private static AppUser user(UUID id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail(id + "@mail.test");
        user.setUsername("user-" + id);
        user.setSystemRole(SystemRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private static Workspace workspace(UUID id, UUID creatorId) {
        Workspace workspace = new Workspace();
        workspace.setId(id);
        workspace.setCreatedBy(user(creatorId));
        return workspace;
    }
}


