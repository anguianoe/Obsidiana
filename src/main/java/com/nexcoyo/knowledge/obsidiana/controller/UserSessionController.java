package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.RevokeAllMyOtherSessionsRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.RevokeSessionRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UserSessionSearchRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserSessionListItemResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserSessionResponse;
import com.nexcoyo.knowledge.obsidiana.facade.UserSessionFacade;
import com.nexcoyo.knowledge.obsidiana.service.UserSessionService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserSessionController {

    private final UserSessionFacade userSessionFacade;
    private final UserSessionService userSessionService;

    @GetMapping("/sessions")
    public PageResponse< UserSessionListItemResponse > searchSessions(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String sessionStatus,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir
    ) {
        return userSessionFacade.search(new UserSessionSearchRequest(userId, sessionStatus, text, null, null, null, activeOnly, page, size, sortBy, sortDir));
    }

    @GetMapping("/users/{userId}/sessions")
    public PageResponse<UserSessionListItemResponse> searchMySessions(
            @PathVariable UUID userId,
            @RequestParam(required = false) String sessionStatus,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir
    ) {
        return userSessionFacade.searchMySessions(userId, new UserSessionSearchRequest(userId, sessionStatus, text, null, null, null, activeOnly, page, size, sortBy, sortDir));
    }

    @GetMapping("/sessions/{sessionId}")
    public UserSessionResponse getSession(
            @PathVariable UUID sessionId,
            @RequestParam(required = false) UUID currentSessionId
    ) {
        return userSessionFacade.getById(sessionId, currentSessionId);
    }

    @PostMapping("/sessions/{sessionId}/revoke")
    public UserSessionResponse revokeSession(
            @PathVariable UUID sessionId,
            @Valid @RequestBody RevokeSessionRequest request,
            @RequestParam(required = false) UUID currentSessionId
    ) {
        return userSessionFacade.revoke(sessionId, request.actorUserId(), currentSessionId, request.reason());
    }

    @PostMapping("/users/{userId}/sessions/revoke-others")
    public Map<String, Object> revokeOtherSessions(
            @PathVariable UUID userId,
            @Valid @RequestBody RevokeAllMyOtherSessionsRequest request
    ) {
        long revokedCount = userSessionService.revokeAllOtherSessions(userId, request.currentSessionId(), request.reason());
        return Map.of(
                "userId", userId,
                "revokedCount", revokedCount,
                "currentSessionId", request.currentSessionId()
        );
    }
}
