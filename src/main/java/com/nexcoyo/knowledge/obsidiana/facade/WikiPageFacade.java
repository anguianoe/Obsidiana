package com.nexcoyo.knowledge.obsidiana.facade;

import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.LinkPageToWorkspaceRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.WikiPageUpsertRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageLinkResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PageTreeNodeResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WikiPageResponse;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPage;
import com.nexcoyo.knowledge.obsidiana.facade.support.ApiMapper;
import com.nexcoyo.knowledge.obsidiana.facade.support.EntityReferenceResolver;
import com.nexcoyo.knowledge.obsidiana.service.WikiPageService;
import com.nexcoyo.knowledge.obsidiana.service.dto.search.WikiPageSearchCriteria;
import com.nexcoyo.knowledge.obsidiana.util.enums.PageStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WikiPageFacade {

    private final WikiPageService wikiPageService;
    private final EntityReferenceResolver refs;

    /** Search wiki pages. Contract: admin flows call with {@code isAdmin=true} and {@code userId=null}. User flows call with {@code isAdmin=false} and session user id. */
    public PageResponse< WikiPageResponse > search( String text, UUID ownerUserId, Boolean encrypted, PageStatus status, Pageable pageable, Boolean isAdmin, UUID userId) {
        WikiPageSearchCriteria criteria = new WikiPageSearchCriteria();
        criteria.setText(text);
        criteria.setOwnerUserId(ownerUserId);
        criteria.setEncrypted(encrypted);
        criteria.setPageStatus(status);
        if ( isAdmin.equals( Boolean.TRUE ) ) {
            return PageResponse.from(wikiPageService.search(criteria, pageable, null), ApiMapper::toResponse);
        }
        else {
            return PageResponse.from(wikiPageService.search(criteria, pageable, userId), ApiMapper::toResponse);
        }

    }

    public PageResponse<WikiPageResponse> searchAccessible(UUID userId, UUID workspaceId, UUID tagId, String searchText, Pageable pageable) {
        return PageResponse.from(wikiPageService.searchAccessible(userId, workspaceId, tagId, searchText, pageable), ApiMapper::toResponse);
    }

    /** Load page by id. Contract: {@code userId=null} means admin context, so user-scoped access checks are skipped. */
    public WikiPageResponse getById(UUID id, UUID userId) {
        if (userId != null) {
            wikiPageService.assertUserPageAccess(id, userId);
        }
        return ApiMapper.toResponse(wikiPageService.getRequired(id));
    }

    /**
     * Save (create or update) a wiki page.
     *
     * @param request     the upsert payload
     * @param ownerUserId user creating/updating (used as ownerUser on create)
     * @param isAdmin     when {@code true} skips page-access check on update
     */
    public WikiPageResponse save( WikiPageUpsertRequest request, UUID ownerUserId, boolean isAdmin) {
        WikiPage entity;
        if (request.id() == null) {
            // Create — ownerUser is always the session user
            entity = new WikiPage();
            entity.setPublicUuid(request.publicUuid() == null ? java.util.UUID.randomUUID() : request.publicUuid());
            entity.setOwnerUser(refs.user(ownerUserId));
        } else {
            // Update — load existing, never overwrite ownerUser
            entity = wikiPageService.getRequired(request.id());
            if (!isAdmin) {
                wikiPageService.assertUserPageAccess(request.id(), ownerUserId);
            }
        }
        entity.setTitle(request.title());
        entity.setSlug(request.slug());
        entity.setEditMode(request.editMode());
        entity.setPageStatus(request.pageStatus());
        entity.setIsEncrypted(request.isEncrypted());
        entity.setIsPublicable(request.isPublicable());
        entity.setCurrentRevision(refs.revision(request.currentRevisionId()));
        return ApiMapper.toResponse(wikiPageService.save(entity));
    }

    /** Link page to workspace. Contract: admin flows set {@code isAdmin=true}; user flows set {@code isAdmin=false}. */
    public PageLinkResponse linkToWorkspace( LinkPageToWorkspaceRequest request, UUID ownerUserId, boolean isAdmin) {
        if (!isAdmin) {
            wikiPageService.assertUserPageAccess(request.pageId(), ownerUserId);
            wikiPageService.assertUserWorkspaceAccess(request.workspaceId(), ownerUserId);
        }
        return ApiMapper.toResponse(wikiPageService.linkToWorkspace(request.pageId(), request.workspaceId(), ownerUserId));
    }

    /** Load workspace page tree. Contract: {@code userId=null} means admin context and bypasses membership validation. */
    public List< PageTreeNodeResponse > tree( UUID workspaceId, UUID parentPageId, UUID userId) {
        if (userId != null) {
            wikiPageService.assertUserWorkspaceAccess(workspaceId, userId);
        }
        return wikiPageService.getTree(workspaceId, parentPageId).stream().map(ApiMapper::toResponse).toList();
    }
}
