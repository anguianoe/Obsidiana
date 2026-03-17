package com.nexcoyo.knowledge.obsidiana.facade;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.dto.request.WikiPageRevisionCreateRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.RevisionSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.WikiPageRevisionResponse;
import com.nexcoyo.knowledge.obsidiana.entity.WikiPageRevision;
import com.nexcoyo.knowledge.obsidiana.facade.support.ApiMapper;
import com.nexcoyo.knowledge.obsidiana.facade.support.EntityReferenceResolver;
import com.nexcoyo.knowledge.obsidiana.service.WikiPageRevisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WikiPageRevisionFacade {

    private final WikiPageRevisionService revisionService;
    private final EntityReferenceResolver refs;

    public WikiPageRevisionResponse getById( UUID revisionId) {
        return ApiMapper.toResponse(revisionService.getRequired(revisionId));
    }

    public WikiPageRevisionResponse latest(UUID pageId) {
        return ApiMapper.toResponse(revisionService.getLatestRevision(pageId));
    }

    public List< RevisionSummaryResponse > summary( UUID pageId) {
        return revisionService.getRevisionSummary(pageId).stream().map(ApiMapper::toResponse).toList();
    }

    public WikiPageRevisionResponse save( WikiPageRevisionCreateRequest request) {
        WikiPageRevision entity = request.id() == null ? new WikiPageRevision() : revisionService.getRequired(request.id());
        entity.setPage(refs.page(request.pageId()));
        entity.setRevisionNumber(request.revisionNumber());
        entity.setTitleSnapshot(request.titleSnapshot());
        entity.setEditorType(request.editorType());
        entity.setContentHtml(request.contentHtml());
        entity.setContentText(request.contentText());
        entity.setChangeSummary(request.changeSummary());
        entity.setIsEncrypted(request.isEncrypted());
        entity.setContentIv(request.contentIv());
        entity.setContentAuthTag(request.contentAuthTag());
        entity.setEncryptionKdf(request.encryptionKdf());
        entity.setIsPinned(request.isPinned());
        entity.setCreatedBy(refs.user(request.createdBy()));
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(OffsetDateTime.now());
        }
        return ApiMapper.toResponse(revisionService.saveRevision(entity, Boolean.TRUE.equals(request.updatePagePointer())));
    }
}
