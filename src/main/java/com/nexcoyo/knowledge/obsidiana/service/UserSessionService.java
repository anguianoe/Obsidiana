package com.nexcoyo.knowledge.obsidiana.service;

import java.util.UUID;
import com.nexcoyo.knowledge.obsidiana.dto.request.UserSessionSearchRequest;
import com.nexcoyo.knowledge.obsidiana.entity.UserSession;
import org.springframework.data.domain.Page;

public interface UserSessionService {

    Page<UserSession> search( UserSessionSearchRequest request);

    Page<UserSession> searchMySessions(UUID userId, UserSessionSearchRequest request);

    UserSession getById(UUID sessionId);

    UserSession revoke( UUID sessionId, UUID actorUserId, String reason);

    long revokeAllOtherSessions(UUID actorUserId, UUID currentSessionId, String reason);
}
