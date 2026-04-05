package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.PublishPageRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UserPublishPageRequest;
import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PublicPagePublicationResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PublicPageSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.facade.PublicationFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/publications")
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationFacade publicationFacade;
    private final GeneralService generalService;

    @GetMapping("/live")
    public PageResponse< PublicPageSummaryResponse > liveSummaries( @PageableDefault(size = 50) Pageable pageable) {
        return publicationFacade.liveSummaries(pageable);
    }

    @GetMapping("/live/by-page/{pageId}")
    public PublicPagePublicationResponse liveByPage( @PathVariable UUID pageId) {
        return publicationFacade.getLiveByPageId(pageId);
    }

    @GetMapping("/live/by-slug/{publicSlug}")
    public PublicPagePublicationResponse liveBySlug(@PathVariable String publicSlug) {
        return publicationFacade.getLiveBySlug(publicSlug);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/public/publish")
    public PublicPagePublicationResponse publish(@Valid @RequestBody UserPublishPageRequest request) {
        UUID userId = generalService.getIdUserFromSession();
        return publicationFacade.publishForUser(request, userId);
    }

    @PostMapping("/admin/publish")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public PublicPagePublicationResponse publishAdmin(@Valid @RequestBody PublishPageRequest request) {
        UUID actorId = generalService.getIdUserFromSession();
        return publicationFacade.publish(request, actorId);
    }
}
