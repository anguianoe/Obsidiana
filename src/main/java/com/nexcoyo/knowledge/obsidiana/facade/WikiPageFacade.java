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

    public PageResponse< WikiPageResponse > search( String text, UUID ownerUserId, Boolean encrypted, PageStatus status, Pageable pageable) {
        WikiPageSearchCriteria criteria = new WikiPageSearchCriteria();
        criteria.setText(text);
        criteria.setOwnerUserId(ownerUserId);
        criteria.setEncrypted(encrypted);
        criteria.setPageStatus(status);
        return PageResponse.from(wikiPageService.search(criteria, pageable), ApiMapper::toResponse);
    }

    public PageResponse<WikiPageResponse> searchAccessible(UUID userId, UUID workspaceId, UUID tagId, String searchText, Pageable pageable) {
        return PageResponse.from(wikiPageService.searchAccessible(userId, workspaceId, tagId, searchText, pageable), ApiMapper::toResponse);
    }

    public WikiPageResponse getById(UUID id) {
        return ApiMapper.toResponse(wikiPageService.getRequired(id));
    }

    public WikiPageResponse save( WikiPageUpsertRequest request, UUID ownerUserId) {

        WikiPage entity = request.id() == null ? new WikiPage() : wikiPageService.getRequired(request.id());
        entity.setPublicUuid(request.publicUuid() == null ? java.util.UUID.randomUUID() : request.publicUuid());
        entity.setOwnerUser(refs.user(ownerUserId));
        entity.setTitle(request.title());
        entity.setSlug(request.slug());
        entity.setEditMode(request.editMode());
        entity.setPageStatus(request.pageStatus());
        entity.setIsEncrypted(request.isEncrypted());
        entity.setIsPublicable(request.isPublicable());
        entity.setCurrentRevision(refs.revision(request.currentRevisionId()));
        return ApiMapper.toResponse(wikiPageService.save(entity));

    }

    public PageLinkResponse linkToWorkspace( LinkPageToWorkspaceRequest request, UUID ownerUserId) {
        return ApiMapper.toResponse(wikiPageService.linkToWorkspace(request.pageId(), request.workspaceId(), ownerUserId));
    }

    public List< PageTreeNodeResponse > tree( UUID workspaceId, UUID parentPageId) {
        return wikiPageService.getTree(workspaceId, parentPageId).stream().map(ApiMapper::toResponse).toList();
    }
}
