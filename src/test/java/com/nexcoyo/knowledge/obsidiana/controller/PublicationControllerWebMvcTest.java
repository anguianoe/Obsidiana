package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.common.dto.PageResponse;
import com.nexcoyo.knowledge.obsidiana.config.SecurityConfig;
import com.nexcoyo.knowledge.obsidiana.dto.response.PublicPageSummaryResponse;
import com.nexcoyo.knowledge.obsidiana.facade.PublicationFacade;
import com.nexcoyo.knowledge.obsidiana.filter.AuthJwtFilter;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicationController.class)
@Import(SecurityConfig.class)
class PublicationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicationFacade publicationFacade;
    @MockitoBean
    private GeneralService generalService;
    @MockitoBean
    private AuthJwtFilter authJwtFilter;

    @BeforeEach
    void allowFilterChainToProceed() throws Exception {
        doAnswer(invocation -> {
            jakarta.servlet.http.HttpServletRequest request = invocation.getArgument(0);
            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            String authHeader = request.getHeader("Authorization");
            if ("Bearer user-token".equals(authHeader)) {
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                        "user",
                        "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    )
                );
            } else if ("Bearer admin-token".equals(authHeader)) {
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                        "admin",
                        "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))
                    )
                );
            }
            chain.doFilter(request, response);
            SecurityContextHolder.clearContext();
            return null;
        }).when(authJwtFilter).doFilter(any(), any(), any());
    }

    @Test
    void liveSummariesPermitAll() throws Exception {
        when(publicationFacade.liveSummaries(any())).thenReturn(new PageResponse<>(List.of(), 0, 50, 0, 0, true, true, true));

        mockMvc.perform(get("/api/v1/publications/live"))
            .andExpect(status().isOk());
    }

    @Test
    void publishUserWithoutAuthReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/publications/public/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUserPublishBody()))
            .andExpect(status().isForbidden());
    }

    @Test
    void publishUserWithRoleIsAllowed() throws Exception {
        when(generalService.getIdUserFromSession()).thenReturn(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/publications/public/publish")
                .header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUserPublishBody()))
            .andExpect(status().isOk());

        verify(publicationFacade).publishForUser(any(), any());
    }

    @Test
    void publishAdminWithUserRoleReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/publications/admin/publish")
                .header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validAdminPublishBody()))
            .andExpect(status().isForbidden());
    }

    @Test
    void publishAdminWithSuperAdminRoleIsAllowed() throws Exception {
        when(generalService.getIdUserFromSession()).thenReturn(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/publications/admin/publish")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validAdminPublishBody()))
            .andExpect(status().isOk());

        verify(publicationFacade).publish(any(), any());
    }

    private String validUserPublishBody() throws Exception {
        return """
            {
              "id": "%s",
              "pageId": "%s",
              "revisionId": "%s",
              "publicSlug": "public-slug",
              "publicTitle": "Public title",
              "publicHtml": "<p>content</p>",
              "publicationStatus": "LIVE"
            }
            """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    }

    private String validAdminPublishBody() throws Exception {
        return """
            {
              "id": "%s",
              "pageId": "%s",
              "revisionId": "%s",
              "publicSlug": "public-slug",
              "publicTitle": "Public title",
              "publicHtml": "<p>content</p>",
              "publicationStatus": "LIVE",
              "publishedBy": "%s"
            }
            """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    }
}






