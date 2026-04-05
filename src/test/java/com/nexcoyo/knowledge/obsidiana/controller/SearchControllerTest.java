package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.facade.SearchFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import java.util.UUID;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchFacade searchFacade;
    @Mock
    private GeneralService generalService;

    @InjectMocks
    private SearchController controller;

    @Test
    void classRequiresUserRole() {
        PreAuthorize preAuthorize = SearchController.class.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('USER')");
    }

    @Test
    void accessiblePagesUsesSessionUser() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        Pageable pageable = Pageable.unpaged();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.accessiblePages(workspaceId, tagId, "wiki", pageable);

        verify(searchFacade).accessiblePages(userId, workspaceId, tagId, "wiki", pageable);
    }

    @Test
    void commentThreadUsesSessionUser() {
        UUID userId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID parentCommentId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.commentThread(pageId, workspaceId, parentCommentId);

        verify(searchFacade).commentThread(userId, pageId, workspaceId, parentCommentId);
    }

    @Test
    void orphanAssetsUsesSessionUser() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = Pageable.unpaged();
        when(generalService.getIdUserFromSession()).thenReturn(userId);

        controller.orphanAssets(pageable);

        verify(searchFacade).orphanAssets(userId, pageable);
    }

    @Test
    void pageableDefaultsAreConfigured() throws NoSuchMethodException {
        Method accessiblePages = SearchController.class.getMethod(
            "accessiblePages",
            UUID.class,
            UUID.class,
            String.class,
            Pageable.class
        );
        Method orphanAssets = SearchController.class.getMethod("orphanAssets", Pageable.class);

        PageableDefault accessibleDefault = findPageableDefault(accessiblePages);
        PageableDefault orphanDefault = findPageableDefault(orphanAssets);

        assertThat(accessibleDefault).isNotNull();
        assertThat(accessibleDefault.size()).isEqualTo(50);
        assertThat(orphanDefault).isNotNull();
        assertThat(orphanDefault.size()).isEqualTo(50);
    }

    private PageableDefault findPageableDefault( Method method) {
        for (Parameter parameter : method.getParameters()) {
            PageableDefault annotation = parameter.getAnnotation(PageableDefault.class);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }
}

