package com.nexcoyo.knowledge.obsidiana.service.specification;

import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.PageComment;
import com.nexcoyo.knowledge.obsidiana.entity.PageWorkspaceLink;
import com.nexcoyo.knowledge.obsidiana.entity.StoredAsset;
import com.nexcoyo.knowledge.obsidiana.entity.TrashRecord;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.repository.TrashRecordRepository;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.TrashRecordSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.util.enums.ApprovalStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssetStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssetType;
import com.nexcoyo.knowledge.obsidiana.util.enums.CommentStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.EditMode;
import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.PageStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.SystemRole;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashEntityType;
import com.nexcoyo.knowledge.obsidiana.util.enums.TrashStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.UserStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceKind;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceRole;
import com.nexcoyo.knowledge.obsidiana.util.enums.WorkspaceStatus;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class TrashRecordSpecificationsTest {

    @Autowired
    private TrashRecordRepository trashRecordRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void visibleToUserReturnsRecordsFromAllSupportedAccessPaths() {
        AppUser user = persistUser("visible-user");
        AppUser other = persistUser("visible-other");

        Workspace workspaceCreatedByUser = persistWorkspace("created-by-user", user);
        Workspace workspaceMember = persistWorkspace("member-workspace", other);
        Workspace pageMembershipWorkspace = persistWorkspace("page-member-workspace", other);
        Workspace commentMembershipWorkspace = persistWorkspace("comment-member-workspace", other);
        Workspace inaccessibleWorkspace = persistWorkspace("inaccessible-workspace", other);

        persistMembership(workspaceMember, user, MembershipStatus.ACTIVE, other);
        persistMembership(pageMembershipWorkspace, user, MembershipStatus.ACTIVE, other);
        persistMembership(commentMembershipWorkspace, user, MembershipStatus.ACTIVE, other);
        persistMembership(inaccessibleWorkspace, user, MembershipStatus.REMOVED, other);

        WikiPage pageOwnedByUser = persistPage(user, "Owned page", "owned-page");
        WikiPage pageAccessibleByMembership = persistPage(other, "Page via membership", "page-member");
        WikiPage inaccessiblePage = persistPage(other, "Inaccessible page", "page-hidden");

        persistPageWorkspaceLink(pageAccessibleByMembership, pageMembershipWorkspace, other);
        persistPageWorkspaceLink(inaccessiblePage, inaccessibleWorkspace, other);

        PageComment commentAuthoredByUser = persistComment(pageOwnedByUser, workspaceCreatedByUser, user, "user comment");
        PageComment commentAccessibleByMembership = persistComment(pageAccessibleByMembership, commentMembershipWorkspace, other, "membership comment");

        StoredAsset assetUploadedByUser = persistAsset(user, "asset-user");
        StoredAsset inaccessibleAsset = persistAsset(other, "asset-other");

        TrashRecord deletedByUser = persistTrashRecord(TrashEntityType.WORKSPACE, UUID.randomUUID(), null, null, null, null, user, TrashStatus.TRASHED, Instant.now().minusSeconds(60));
        TrashRecord workspaceCreatorVisible = persistTrashRecord(TrashEntityType.WORKSPACE, workspaceCreatedByUser.getId(), workspaceCreatedByUser, null, null, null, other, TrashStatus.TRASHED, Instant.now().plusSeconds(3600));
        TrashRecord workspaceMemberVisible = persistTrashRecord(TrashEntityType.WORKSPACE, workspaceMember.getId(), workspaceMember, null, null, null, other, TrashStatus.TRASHED, Instant.now().plusSeconds(3600));
        TrashRecord pageOwnerVisible = persistTrashRecord(TrashEntityType.PAGE, pageOwnedByUser.getId(), null, pageOwnedByUser, null, null, other, TrashStatus.TRASHED, Instant.now().plusSeconds(3600));
        TrashRecord pageMembershipVisible = persistTrashRecord(TrashEntityType.PAGE, pageAccessibleByMembership.getId(), null, pageAccessibleByMembership, null, null, other, TrashStatus.TRASHED, Instant.now().plusSeconds(3600));
        TrashRecord commentAuthorVisible = persistTrashRecord(TrashEntityType.COMMENT, commentAuthoredByUser.getId(), workspaceCreatedByUser, pageOwnedByUser, null, commentAuthoredByUser, other, TrashStatus.TRASHED, Instant.now().plusSeconds(3600));
        TrashRecord commentMembershipVisible = persistTrashRecord(TrashEntityType.COMMENT, commentAccessibleByMembership.getId(), commentMembershipWorkspace, pageAccessibleByMembership, null, commentAccessibleByMembership, other, TrashStatus.TRASHED, Instant.now().plusSeconds(3600));
        TrashRecord assetUploaderVisible = persistTrashRecord(TrashEntityType.ASSET, assetUploadedByUser.getId(), null, null, assetUploadedByUser, null, other, TrashStatus.TRASHED, Instant.now().plusSeconds(3600));
        TrashRecord inaccessible = persistTrashRecord(TrashEntityType.ASSET, inaccessibleAsset.getId(), inaccessibleWorkspace, inaccessiblePage, inaccessibleAsset, null, other, TrashStatus.TRASHED, Instant.now().plusSeconds(3600));

        flushAndClear();

        List<TrashRecord> result = trashRecordRepository.findAll(TrashRecordSpecifications.visibleToUser(new TrashRecordSearchCriteria(), user.getId()));

        assertThat(ids(result)).containsExactlyInAnyOrder(
            deletedByUser.getId(),
            workspaceCreatorVisible.getId(),
            workspaceMemberVisible.getId(),
            pageOwnerVisible.getId(),
            pageMembershipVisible.getId(),
            commentAuthorVisible.getId(),
            commentMembershipVisible.getId(),
            assetUploaderVisible.getId()
        );
        assertThat(ids(result)).doesNotContain(inaccessible.getId());
    }

    @Test
    void visibleToUserWithNullUserFallsBackToCriteriaOnlyFiltering() {
        AppUser owner = persistUser("null-user-owner");
        Workspace workspace = persistWorkspace("null-user-workspace", owner);
        WikiPage page = persistPage(owner, "Null User Page", "null-user-page");
        persistPageWorkspaceLink(page, workspace, owner);

        TrashRecord matching = persistTrashRecord(TrashEntityType.PAGE, page.getId(), workspace, page, null, null, owner, TrashStatus.TRASHED, Instant.now().minusSeconds(300));
        TrashRecord wrongStatus = persistTrashRecord(TrashEntityType.PAGE, page.getId(), workspace, page, null, null, owner, TrashStatus.RESTORED, Instant.now().minusSeconds(300));
        TrashRecord wrongWorkspace = persistTrashRecord(TrashEntityType.PAGE, page.getId(), persistWorkspace("other-workspace", owner), page, null, null, owner, TrashStatus.TRASHED, Instant.now().minusSeconds(300));

        flushAndClear();

        TrashRecordSearchCriteria criteria = new TrashRecordSearchCriteria();
        criteria.setWorkspaceId(workspace.getId());
        criteria.setEntityType(TrashEntityType.PAGE);
        criteria.setStatus(TrashStatus.TRASHED);
        criteria.setOverdue(true);

        List<TrashRecord> result = trashRecordRepository.findAll(TrashRecordSpecifications.visibleToUser(criteria, null));

        assertThat(ids(result)).containsExactly(matching.getId());
        assertThat(ids(result)).doesNotContain(wrongStatus.getId(), wrongWorkspace.getId());
    }

    @Test
    void byCriteriaCombinesWorkspacePageAssetDeletedByTypeStatusAndOverdue() {
        AppUser owner = persistUser("criteria-owner");
        AppUser deletedBy = persistUser("criteria-deleted-by");
        Workspace workspace = persistWorkspace("criteria-workspace", owner);
        WikiPage page = persistPage(owner, "Criteria Page", "criteria-page");
        StoredAsset asset = persistAsset(owner, "criteria-asset");
        persistPageWorkspaceLink(page, workspace, owner);

        TrashRecord target = persistTrashRecord(TrashEntityType.ASSET, asset.getId(), workspace, page, asset, null, deletedBy, TrashStatus.TRASHED, Instant.now().minusSeconds(600));
        TrashRecord wrongDeletedBy = persistTrashRecord(TrashEntityType.ASSET, asset.getId(), workspace, page, asset, null, owner, TrashStatus.TRASHED, Instant.now().minusSeconds(600));
        TrashRecord wrongStatus = persistTrashRecord(TrashEntityType.ASSET, asset.getId(), workspace, page, asset, null, deletedBy, TrashStatus.RESTORED, Instant.now().minusSeconds(600));
        TrashRecord wrongOverdue = persistTrashRecord(TrashEntityType.ASSET, asset.getId(), workspace, page, asset, null, deletedBy, TrashStatus.TRASHED, Instant.now().plusSeconds(600));
        TrashRecord wrongType = persistTrashRecord(TrashEntityType.PAGE, page.getId(), workspace, page, null, null, deletedBy, TrashStatus.TRASHED, Instant.now().minusSeconds(600));

        flushAndClear();

        TrashRecordSearchCriteria criteria = new TrashRecordSearchCriteria();
        criteria.setWorkspaceId(workspace.getId());
        criteria.setPageId(page.getId());
        criteria.setAssetId(asset.getId());
        criteria.setDeletedBy(deletedBy.getId());
        criteria.setEntityType(TrashEntityType.ASSET);
        criteria.setStatus(TrashStatus.TRASHED);
        criteria.setOverdue(true);

        List<TrashRecord> result = trashRecordRepository.findAll(TrashRecordSpecifications.byCriteria(criteria));

        assertThat(ids(result)).containsExactly(target.getId());
        assertThat(ids(result)).doesNotContain(
            wrongDeletedBy.getId(),
            wrongStatus.getId(),
            wrongOverdue.getId(),
            wrongType.getId()
        );
    }

    @Test
    void visibleToUserStillHonorsAdditionalCriteriaFilters() {
        AppUser user = persistUser("criteria-visible-user");
        AppUser other = persistUser("criteria-visible-other");
        Workspace workspace = persistWorkspace("criteria-visible-workspace", other);
        Workspace otherWorkspace = persistWorkspace("criteria-visible-other-workspace", other);
        persistMembership(workspace, user, MembershipStatus.ACTIVE, other);
        persistMembership(otherWorkspace, user, MembershipStatus.ACTIVE, other);

        WikiPage page = persistPage(other, "Visible Page", "visible-page");
        WikiPage otherPage = persistPage(other, "Other Page", "other-page");
        persistPageWorkspaceLink(page, workspace, other);
        persistPageWorkspaceLink(otherPage, otherWorkspace, other);

        StoredAsset targetAsset = persistAsset(other, "visible-asset");
        StoredAsset otherAsset = persistAsset(other, "other-asset");

        TrashRecord matching = persistTrashRecord(TrashEntityType.ASSET, targetAsset.getId(), workspace, page, targetAsset, null, other, TrashStatus.TRASHED, Instant.now().minusSeconds(120));
        TrashRecord wrongAsset = persistTrashRecord(TrashEntityType.ASSET, otherAsset.getId(), workspace, page, otherAsset, null, other, TrashStatus.TRASHED, Instant.now().minusSeconds(120));
        TrashRecord wrongWorkspace = persistTrashRecord(TrashEntityType.ASSET, targetAsset.getId(), otherWorkspace, otherPage, targetAsset, null, other, TrashStatus.TRASHED, Instant.now().minusSeconds(120));
        TrashRecord wrongNotOverdue = persistTrashRecord(TrashEntityType.ASSET, targetAsset.getId(), workspace, page, targetAsset, null, other, TrashStatus.TRASHED, Instant.now().plusSeconds(120));

        flushAndClear();

        TrashRecordSearchCriteria criteria = new TrashRecordSearchCriteria();
        criteria.setWorkspaceId(workspace.getId());
        criteria.setPageId(page.getId());
        criteria.setAssetId(targetAsset.getId());
        criteria.setEntityType(TrashEntityType.ASSET);
        criteria.setStatus(TrashStatus.TRASHED);
        criteria.setOverdue(true);

        List<TrashRecord> result = trashRecordRepository.findAll(TrashRecordSpecifications.visibleToUser(criteria, user.getId()));

        assertThat(ids(result)).containsExactly(matching.getId());
        assertThat(ids(result)).doesNotContain(wrongAsset.getId(), wrongWorkspace.getId(), wrongNotOverdue.getId());
    }

    private AppUser persistUser(String suffix) {
        AppUser user = new AppUser();
        user.setEmail(suffix + "@test.com");
        user.setUsername(suffix + "-" + UUID.randomUUID());
        user.setPasswordHash("hash-" + suffix);
        user.setSystemRole(SystemRole.USER);
        user.setRoles("USER");
        user.setStatus(UserStatus.ACTIVE);
        user.setHasCompletedOnboarding(Boolean.TRUE);
        user.setOnboardingVersion("1.0");
        entityManager.persist(user);
        return user;
    }

    private Workspace persistWorkspace(String suffix, AppUser createdBy) {
        Workspace workspace = new Workspace();
        workspace.setName("Workspace " + suffix);
        workspace.setSlug("workspace-" + suffix + "-" + UUID.randomUUID());
        workspace.setKind(WorkspaceKind.GROUP);
        workspace.setStatus(WorkspaceStatus.ACTIVE);
        workspace.setApprovalStatus(ApprovalStatus.APPROVED);
        workspace.setCreatedBy(createdBy);
        entityManager.persist(workspace);
        return workspace;
    }

    private WikiPage persistPage(AppUser owner, String title, String slug) {
        WikiPage page = new WikiPage();
        page.setPublicUuid(UUID.randomUUID());
        page.setOwnerUser(owner);
        page.setTitle(title);
        page.setSlug(slug + "-" + UUID.randomUUID());
        page.setEditMode(EditMode.SHARED);
        page.setPageStatus(PageStatus.ACTIVE);
        page.setIsEncrypted(false);
        page.setIsPublicable(true);
        entityManager.persist(page);
        return page;
    }

    private void persistPageWorkspaceLink(WikiPage page, Workspace workspace, AppUser linkedBy) {
        PageWorkspaceLink link = new PageWorkspaceLink();
        link.setPage(page);
        link.setWorkspace(workspace);
        link.setLinkedAt(OffsetDateTime.now());
        link.setLinkedBy(linkedBy);
        entityManager.persist(link);
    }

    private void persistMembership(Workspace workspace, AppUser user, MembershipStatus status, AppUser createdBy) {
        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setWorkspace(workspace);
        membership.setUser(user);
        membership.setRole(WorkspaceRole.EDITOR);
        membership.setStatus(status);
        membership.setCreatedBy(createdBy);
        membership.setJoinedAt(Instant.now());
        entityManager.persist(membership);
    }

    private PageComment persistComment(WikiPage page, Workspace workspace, AppUser author, String body) {
        PageComment comment = new PageComment();
        comment.setPage(page);
        comment.setWorkspace(workspace);
        comment.setAuthorUser(author);
        comment.setBody(body);
        comment.setCommentStatus(CommentStatus.ACTIVE);
        comment.setCreatedAt(OffsetDateTime.now());
        entityManager.persist(comment);
        return comment;
    }

    private StoredAsset persistAsset(AppUser uploadedBy, String suffix) {
        StoredAsset asset = new StoredAsset();
        asset.setStorageProvider("local");
        asset.setBucketName("bucket");
        asset.setObjectKey("obj-" + suffix + "-" + UUID.randomUUID());
        asset.setOriginalFilename(suffix + ".png");
        asset.setNormalizedFilename(suffix + ".png");
        asset.setMimeType("image/png");
        asset.setAssetType(AssetType.IMAGE);
        asset.setFileExtension("png");
        asset.setSizeBytes(128L);
        asset.setChecksumSha256("a".repeat(64));
        asset.setStatus(AssetStatus.ACTIVE);
        asset.setUploadedBy(uploadedBy);
        entityManager.persist(asset);
        return asset;
    }

    private TrashRecord persistTrashRecord(
        TrashEntityType entityType,
        UUID entityId,
        Workspace workspace,
        WikiPage page,
        StoredAsset asset,
        PageComment comment,
        AppUser deletedBy,
        TrashStatus status,
        Instant restoreDeadlineAt
    ) {
        TrashRecord trashRecord = new TrashRecord();
        trashRecord.setEntityType(entityType);
        trashRecord.setEntityId(entityId);
        trashRecord.setWorkspace(workspace);
        trashRecord.setPage(page);
        trashRecord.setAsset(asset);
        trashRecord.setComment(comment);
        trashRecord.setDeletedBy(deletedBy);
        trashRecord.setDeleteReason("cleanup");
        trashRecord.setDeletedAt(Instant.now().minusSeconds(300));
        trashRecord.setRestoreDeadlineAt(restoreDeadlineAt);
        trashRecord.setStatus(status);
        entityManager.persist(trashRecord);
        return trashRecord;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private Set<UUID> ids(List<TrashRecord> records) {
        return records.stream().map(TrashRecord::getId).collect(Collectors.toSet());
    }
}


