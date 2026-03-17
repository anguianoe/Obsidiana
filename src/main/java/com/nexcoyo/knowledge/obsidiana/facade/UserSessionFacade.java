package com.nexcoyo.knowledge.obsidiana.facade;

import java.util.UUID;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.dto.request.UserSessionSearchRequest;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserSessionListItemResponse;
import com.nexcoyo.knowledge.obsidiana.dto.response.UserSessionResponse;
import com.nexcoyo.knowledge.obsidiana.entity.UserSession;
import com.nexcoyo.knowledge.obsidiana.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSessionFacade {

    private final UserSessionService userSessionService;

    public PageResponse<UserSessionListItemResponse> search(UserSessionSearchRequest request) {
        return PageResponse.from(userSessionService.search(request), this::toListItem);
    }

    public PageResponse<UserSessionListItemResponse> searchMySessions(UUID userId, UserSessionSearchRequest request) {
        return PageResponse.from(userSessionService.searchMySessions(userId, request), this::toListItem);
    }

    public UserSessionResponse getById(UUID sessionId, UUID currentSessionId) {
        return toResponse(userSessionService.getById(sessionId), currentSessionId);
    }

    public UserSessionResponse revoke(UUID sessionId, UUID actorUserId, UUID currentSessionId, String reason) {
        return toResponse(userSessionService.revoke(sessionId, actorUserId, reason), currentSessionId);
    }

    public UserSessionResponse toResponse(UserSession session, UUID currentSessionId) {
        boolean current = currentSessionId != null && currentSessionId.equals(session.getId());
        boolean revocable = !current && session.getRevokedAt() == null;
        return new UserSessionResponse(
                session.getId(),
                session.getUser().getId(),
                session.getSessionStatus() == null ? null : session.getSessionStatus().name(),
                session.getIpAddress(),
                session.getUserAgent(),
                session.getDeviceType(),
                session.getOsName(),
                session.getBrowserName(),
                session.getCityName(),
                session.getRegionName(),
                session.getCountryName(),
                session.getLoginAt(),
                session.getExpiresAt(),
                session.getRevokedAt(),
                session.getCreatedAt(),
                current,
                revocable
        );
    }

    public UserSessionListItemResponse toListItem(UserSession session) {
        return new UserSessionListItemResponse(
                session.getId(),
                session.getSessionStatus() == null ? null : session.getSessionStatus().name(),
                session.getDeviceType(),
                session.getBrowserName(),
                session.getOsName(),
                session.getCityName(),
                session.getRegionName(),
                session.getCountryName(),
                session.getLoginAt(),
                session.getExpiresAt(),
                session.getRevokedAt()
        );
    }
}
