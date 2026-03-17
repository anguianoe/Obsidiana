package com.nexcoyo.knowledge.obsidiana.filter;


import com.nexcoyo.knowledge.obsidiana.records.AuthUser;
import com.nexcoyo.knowledge.obsidiana.service.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class AuthJwtFilter extends OncePerRequestFilter
{

    private final JwtService jwtService;

    public AuthJwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException
    {

        String auth = request.getHeader( HttpHeaders.AUTHORIZATION);

        if (auth == null || !auth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = auth.substring("Bearer ".length()).trim();

        try {
            Claims claims = jwtService.parse(token);

            if (!jwtService.isAccessToken(claims)) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token type");
                return;
            }

            String email = claims.getSubject();
            if (email == null || email.isBlank()) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing subject");
                return;
            }

            @SuppressWarnings("unchecked") List<String> roles = claims.get("roles", List.class);

            var authorities = (roles == null ? List.<String>of() : roles).stream()
                                                                         .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                                                                         .map( SimpleGrantedAuthority::new)
                                                                         .toList();

            Object idUserClaim = claims.get("idUser");
            UUID userId = null;
            if (idUserClaim instanceof UUID uuid) {
                userId = uuid;
            } else if (idUserClaim instanceof String raw && !raw.isBlank()) {
                userId = UUID.fromString(raw);
            }

            AuthUser principal = new AuthUser(userId, email);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch ( ExpiredJwtException e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
        } catch ( JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        }
    }
}
