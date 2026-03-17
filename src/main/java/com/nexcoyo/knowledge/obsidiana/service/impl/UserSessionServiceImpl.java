package com.nexcoyo.knowledge.obsidiana.service.impl;

import com.nexcoyo.knowledge.obsidiana.dto.request.UserSessionSearchRequest;
import com.nexcoyo.knowledge.obsidiana.entity.UserSession;
import com.nexcoyo.knowledge.obsidiana.repository.UserSessionRepository;
import com.nexcoyo.knowledge.obsidiana.service.UserSessionService;
import com.nexcoyo.knowledge.obsidiana.service.specification.UserSessionSpecifications;
import com.nexcoyo.knowledge.obsidiana.util.enums.SessionStatus;
import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSessionServiceImpl implements UserSessionService
{

    private final UserSessionRepository userSessionRepository;

    @Override
    public Page<UserSession> search( UserSessionSearchRequest request) {
        Specification< UserSession > specification = UserSessionSpecifications.from(request);
        return userSessionRepository.findAll(specification, pageable(request));
    }

    @Override
    public Page<UserSession> searchMySessions(UUID userId, UserSessionSearchRequest request) {
        UserSessionSearchRequest scoped = new UserSessionSearchRequest(
                userId,
                request == null ? null : request.sessionStatus(),
                request == null ? null : request.text(),
                request == null ? null : request.loginFrom(),
                request == null ? null : request.loginTo(),
                request == null ? null : request.expiresBefore(),
                request == null ? Boolean.TRUE : request.activeOnly(),
                request == null ? 0 : request.page(),
                request == null ? 20 : request.size(),
                request == null ? "loginAt" : request.sortBy(),
                request == null ? "desc" : request.sortDir()
        );
        return search(scoped);
    }

    @Override
    public UserSession getById(UUID sessionId) {
        return userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));
    }

    @Override
    @Transactional
    public UserSession revoke(UUID sessionId, UUID actorUserId, String reason) {
        UserSession session = getById(sessionId);
        if (session.getSessionStatus() == SessionStatus.REVOKED) {
            return session;
        }
        if (!session.getUser().getId().equals(actorUserId)) {
            throw new IllegalArgumentException("Only the owner can revoke this session in the bootstrap version");
        }
        session.setSessionStatus(SessionStatus.REVOKED);
        session.setRevokedAt(OffsetDateTime.now());
        return userSessionRepository.save(session);
    }

    @Override
    @Transactional
    public long revokeAllOtherSessions(UUID actorUserId, UUID currentSessionId, String reason) {
        return userSessionRepository.bulkRevokeOtherActiveSessions(actorUserId, currentSessionId, OffsetDateTime.now());
    }

    private Pageable pageable(UserSessionSearchRequest request) {
        int page = request != null && request.page() != null ? Math.max(request.page(), 0) : 0;
        int size = request != null && request.size() != null ? Math.min(Math.max(request.size(), 1), 200) : 20;
        String sortBy = request != null && request.sortBy() != null && !request.sortBy().isBlank() ? request.sortBy() : "loginAt";
        Sort.Direction direction = request != null && "asc".equalsIgnoreCase(request.sortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
