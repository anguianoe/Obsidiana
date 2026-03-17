package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.PublishPageRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.PublicPagePublicationResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.PublicPageSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.facade.PublicationFacade;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/publications")
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationFacade publicationFacade;

    @GetMapping("/live")
    public List< PublicPageSummaryResponse > liveSummaries() {
        return publicationFacade.liveSummaries();
    }

    @GetMapping("/live/by-page/{pageId}")
    public PublicPagePublicationResponse liveByPage( @PathVariable UUID pageId) {
        return publicationFacade.getLiveByPageId(pageId);
    }

    @GetMapping("/live/by-slug/{publicSlug}")
    public PublicPagePublicationResponse liveBySlug(@PathVariable String publicSlug) {
        return publicationFacade.getLiveBySlug(publicSlug);
    }

    @PostMapping
    public PublicPagePublicationResponse publish(@Valid @RequestBody PublishPageRequest request) {
        return publicationFacade.publish(request);
    }
}
