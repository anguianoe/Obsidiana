package com.nexcoyo.knowledge.obsidiana.service.specification;

import com.nexcoyo.knowledge.obsidiana.entity.AppUser;
import com.nexcoyo.knowledge.obsidiana.entity.PageHierarchy;
import com.nexcoyo.knowledge.obsidiana.entity.PageTagAssignment;
import com.nexcoyo.knowledge.obsidiana.entity.PageWorkspaceLink;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.entity.Workspace;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceMembership;
import com.nexcoyo.knowledge.obsidiana.entity.WorkspaceTag;
import com.nexcoyo.knowledge.obsidiana.repository.WikiPageRepository;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.WikiPageSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.util.enums.ApprovalStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.AssignmentStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.EditMode;
import com.nexcoyo.knowledge.obsidiana.util.enums.MembershipStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.PageStatus;
import com.nexcoyo.knowledge.obsidiana.util.enums.SystemRole;
import com.nexcoyo.knowledge.obsidiana.util.enums.TagStatus;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class WikiPageSpecificationsTest {

    @Autowired
    private WikiPageRepository wikiPageRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void accessibleToUserReturnsOwnedAndMembershipAccessiblePagesOnly() {
        AppUser member = persistUser("member");
        AppUser otherOwner = persistUser("other-owner");

        Workspace activeWorkspace = persistWorkspace("active-workspace", otherOwner);
        Workspace inactiveWorkspace = persistWorkspace("inactive-workspace", otherOwner);

        WikiPage ownedPage = persistPage(member, "Owned Page", "owned-page", EditMode.SHARED, PageStatus.ACTIVE, false, true);
        WikiPage membershipPage = persistPage(otherOwner, "Membership Page", "membership-page", EditMode.SHARED, PageStatus.ACTIVE, false, true);
        WikiPage inactiveMembershipPage = persistPage(otherOwner, "Inactive Membership", "inactive-membership", EditMode.SHARED, PageStatus.ACTIVE, false, true);
        WikiPage unrelatedPage = persistPage(otherOwner, "Unrelated Page", "unrelated-page", EditMode.SHARED, PageStatus.ACTIVE, false, true);

        persistPageWorkspaceLink(membershipPage, activeWorkspace, otherOwner);
        persistPageWorkspaceLink(inactiveMembershipPage, inactiveWorkspace, otherOwner);
        persistMembership(activeWorkspace, member, MembershipStatus.ACTIVE, otherOwner);
        persistMembership(inactiveWorkspace, member, MembershipStatus.REMOVED, otherOwner);

        flushAndClear();

        List<WikiPage> result = wikiPageRepository.findAll(WikiPageSpecifications.accessibleToUser(member.getId()));

        assertThat(ids(result)).containsExactlyInAnyOrder(ownedPage.getId(), membershipPage.getId());
        assertThat(ids(result)).doesNotContain(inactiveMembershipPage.getId(), unrelatedPage.getId());
    }

    @Test
    void accessibleToUserWithNullUserReturnsAllPages() {
        AppUser owner = persistUser("owner");
        WikiPage first = persistPage(owner, "First", "first", EditMode.SHARED, PageStatus.ACTIVE, false, true);
        WikiPage second = persistPage(owner, "Second", "second", EditMode.OWNER_ONLY, PageStatus.ARCHIVED, true, false);

        flushAndClear();

        List<WikiPage> result = wikiPageRepository.findAll(WikiPageSpecifications.accessibleToUser(null));

        assertThat(ids(result)).containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void textMatchesTitleAndSlugCaseInsensitiveAndIgnoresBlank() {
        AppUser owner = persistUser("owner");
        WikiPage titleMatch = persistPage(owner, "Alpha Knowledge", "knowledge-home", EditMode.SHARED, PageStatus.ACTIVE, false, true);
        WikiPage slugMatch = persistPage(owner, "Beta Page", "my-special-slug", EditMode.SHARED, PageStatus.ACTIVE, false, true);
        WikiPage unmatched = persistPage(owner, "Gamma Page", "gamma-page", EditMode.SHARED, PageStatus.ACTIVE, false, true);

        flushAndClear();

        List<WikiPage> byTitle = wikiPageRepository.findAll(WikiPageSpecifications.text("ALPHA"));
        List<WikiPage> bySlug = wikiPageRepository.findAll(WikiPageSpecifications.text("SPECIAL-SLUG"));
        List<WikiPage> byBlank = wikiPageRepository.findAll(WikiPageSpecifications.text("   "));

        assertThat(ids(byTitle)).containsExactly(titleMatch.getId());
        assertThat(ids(bySlug)).containsExactly(slugMatch.getId());
        assertThat(ids(byBlank)).containsExactlyInAnyOrder(titleMatch.getId(), slugMatch.getId(), unmatched.getId());
    }

    @Test
    void rootPagesAndChildOfFilterWithinWorkspaceHierarchy() {
        AppUser owner = persistUser("owner");
        Workspace workspace = persistWorkspace("workspace", owner);
        Workspace otherWorkspace = persistWorkspace("other-workspace", owner);

        WikiPage parent = persistPage(owner, "Parent", "parent", EditMode.SHARED, PageStatus.ACTIVE, false, true);
        WikiPage child = persistPage(owner, "Child", "child", EditMode.SHARED, PageStatus.ACTIVE, false, true);
        WikiPage siblingRoot = persistPage(owner, "Sibling Root", "sibling-root", EditMode.SHARED, PageStatus.ACTIVE, false, true);
        WikiPage external = persistPage(owner, "External", "external", EditMode.SHARED, PageStatus.ACTIVE, false, true);

        persistPageWorkspaceLink(parent, workspace, owner);
        persistPageWorkspaceLink(child, workspace, owner);
        persistPageWorkspaceLink(siblingRoot, workspace, owner);
        persistPageWorkspaceLink(external, otherWorkspace, owner);
        persistHierarchy(parent, child, workspace);

        flushAndClear();

        Specification<WikiPage> rootsInWorkspace = Specification.allOf(
            WikiPageSpecifications.workspace(workspace.getId()),
            WikiPageSpecifications.rootPages(true, workspace.getId())
        );
        Specification<WikiPage> childrenOfParent = Specification.allOf(
            WikiPageSpecifications.workspace(workspace.getId()),
            WikiPageSpecifications.childOf(parent.getId(), workspace.getId())
        );

        List<WikiPage> rootResults = wikiPageRepository.findAll(rootsInWorkspace);
        List<WikiPage> childResults = wikiPageRepository.findAll(childrenOfParent);

        assertThat(ids(rootResults)).containsExactlyInAnyOrder(parent.getId(), siblingRoot.getId());
        assertThat(ids(rootResults)).doesNotContain(child.getId(), external.getId());
        assertThat(ids(childResults)).containsExactly(child.getId());
    }

    @Test
    void byCriteriaCombinesOwnerWorkspaceTagTextAndFlags() {
        AppUser owner = persistUser("owner");
        AppUser otherOwner = persistUser("other-owner");
        Workspace workspace = persistWorkspace("workspace", owner);
        Workspace otherWorkspace = persistWorkspace("other-workspace", owner);

        WikiPage target = persistPage(owner, "Architecture Alpha", "alpha-architecture", EditMode.SHARED, PageStatus.ACTIVE, true, false);
        WikiPage wrongTag = persistPage(owner, "Architecture Alpha Two", "alpha-two", EditMode.SHARED, PageStatus.ACTIVE, true, false);
        WikiPage wrongWorkspace = persistPage(owner, "Architecture Alpha Three", "alpha-three", EditMode.SHARED, PageStatus.ACTIVE, true, false);
        WikiPage wrongOwner = persistPage(otherOwner, "Architecture Alpha Four", "alpha-four", EditMode.SHARED, PageStatus.ACTIVE, true, false);
        WikiPage wrongFlags = persistPage(owner, "Architecture Alpha Five", "alpha-five", EditMode.OWNER_ONLY, PageStatus.ARCHIVED, false, true);

        WorkspaceTag targetTag = persistWorkspaceTag(workspace, "platform", owner);
        WorkspaceTag otherTag = persistWorkspaceTag(workspace, "mobile", owner);

        persistPageWorkspaceLink(target, workspace, owner);
        persistPageWorkspaceLink(wrongTag, workspace, owner);
        persistPageWorkspaceLink(wrongWorkspace, otherWorkspace, owner);
        persistPageWorkspaceLink(wrongOwner, workspace, owner);
        persistPageWorkspaceLink(wrongFlags, workspace, owner);

        persistPageTagAssignment(target, workspace, targetTag, owner);
        persistPageTagAssignment(wrongTag, workspace, otherTag, owner);
        persistPageTagAssignment(wrongOwner, workspace, targetTag, owner);

        flushAndClear();

        WikiPageSearchCriteria criteria = new WikiPageSearchCriteria();
        criteria.setOwnerUserId(owner.getId());
        criteria.setWorkspaceId(workspace.getId());
        criteria.setTagId(targetTag.getId());
        criteria.setText("ALPHA");
        criteria.setPageStatus(PageStatus.ACTIVE);
        criteria.setEditMode(EditMode.SHARED);
        criteria.setEncrypted(true);
        criteria.setPublicable(false);

        List<WikiPage> result = wikiPageRepository.findAll(WikiPageSpecifications.byCriteria(criteria));

        assertThat(ids(result)).containsExactly(target.getId());
    }

    private AppUser persistUser(String suffix) {
        AppUser user = new AppUser();
        user.setEmail(suffix + "@test.com");
        user.setUsername(suffix);
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

    private WikiPage persistPage(
        AppUser owner,
        String title,
        String slug,
        EditMode editMode,
        PageStatus status,
        boolean encrypted,
        boolean publicable
    ) {
        WikiPage page = new WikiPage();
        page.setPublicUuid(UUID.randomUUID());
        page.setOwnerUser(owner);
        page.setTitle(title);
        page.setSlug(slug + "-" + UUID.randomUUID());
        page.setEditMode(editMode);
        page.setPageStatus(status);
        page.setIsEncrypted(encrypted);
        page.setIsPublicable(publicable);
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

    private void persistHierarchy(WikiPage parent, WikiPage child, Workspace workspace) {
        PageHierarchy hierarchy = new PageHierarchy();
        hierarchy.setParentPage(parent);
        hierarchy.setChildPage(child);
        hierarchy.setWorkspace(workspace);
        hierarchy.setSortOrder(1);
        hierarchy.setCreatedAt(OffsetDateTime.now());
        entityManager.persist(hierarchy);
    }

    private WorkspaceTag persistWorkspaceTag(Workspace workspace, String name, AppUser createdBy) {
        WorkspaceTag tag = new WorkspaceTag();
        tag.setWorkspace(workspace);
        tag.setName(name + "-" + UUID.randomUUID());
        tag.setTagStatus(TagStatus.ACTIVE);
        tag.setCreatedBy(createdBy);
        tag.setCreatedAt(OffsetDateTime.now());
        entityManager.persist(tag);
        return tag;
    }

    private void persistPageTagAssignment(WikiPage page, Workspace workspace, WorkspaceTag tag, AppUser createdBy) {
        PageTagAssignment assignment = new PageTagAssignment();
        assignment.setPage(page);
        assignment.setWorkspace(workspace);
        assignment.setTag(tag);
        assignment.setAssignmentStatus(AssignmentStatus.ACTIVE);
        assignment.setCreatedBy(createdBy);
        assignment.setCreatedAt(OffsetDateTime.now());
        entityManager.persist(assignment);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private Set<UUID> ids(List<WikiPage> pages) {
        return pages.stream().map(WikiPage::getId).collect(java.util.stream.Collectors.toSet());
    }
}



