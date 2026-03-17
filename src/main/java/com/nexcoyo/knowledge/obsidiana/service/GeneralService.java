package com.nexcoyo.knowledge.obsidiana.service;

import com.nexcoyo.knowledge.obsidiana.records.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GeneralService
{
    public UUID getIdUserFromSession() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in security context");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof AuthUser user)) {
            throw new IllegalStateException("Principal is not AuthUser: " + principal);
        }

        return user.id();
    }

    public static String userAgent( HttpServletRequest r) {
        String ua = r.getHeader("User-Agent");
        return ua == null ? "" : ua;
    }

    public static String clientIp(HttpServletRequest r) {
        return r.getRemoteAddr();
    }
}
