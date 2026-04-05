package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.facade.SearchFacade;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchAdminControllerTest {

    @Mock
    private SearchFacade searchFacade;

    @InjectMocks
    private SearchAdminController controller;

    @Test
    void classRequiresSuperAdminRole() {
        PreAuthorize preAuthorize = SearchAdminController.class.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('SUPER_ADMIN')");
    }

    @Test
    void accessiblePagesDelegatesToFacade() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        Pageable pageable = Pageable.unpaged();

        controller.accessiblePages(userId, workspaceId, tagId, "text", pageable);

        verify(searchFacade).accessiblePages(userId, workspaceId, tagId, "text", pageable);
    }

    @Test
    void commentThreadDelegatesToFacade() {
        UUID pageId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID parentCommentId = UUID.randomUUID();

        controller.commentThread(null, pageId, workspaceId, parentCommentId);

        verify(searchFacade).commentThread(pageId, workspaceId, parentCommentId);
    }

    @Test
    void commentThreadWithUserIdUsesUserScopedFacade() {
        UUID userId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();

        controller.commentThread(userId, pageId, workspaceId, null);

        verify(searchFacade).commentThread(userId, pageId, workspaceId, null);
        verify(searchFacade, never()).commentThread(pageId, workspaceId, null);
    }

    @Test
    void orphanAssetsDelegatesToFacade() {
        Pageable pageable = Pageable.unpaged();

        controller.orphanAssets(null, pageable);

        verify(searchFacade).orphanAssets(pageable);
    }

    @Test
    void orphanAssetsWithUserIdUsesUserScopedFacade() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = Pageable.unpaged();

        controller.orphanAssets(userId, pageable);

        verify(searchFacade).orphanAssets(userId, pageable);
        verify(searchFacade, never()).orphanAssets(pageable);
    }

    @Test
    void pageableDefaultsAreConfigured() throws NoSuchMethodException {
        Method accessiblePages = SearchAdminController.class.getMethod(
            "accessiblePages",
            UUID.class,
            UUID.class,
            UUID.class,
            String.class,
            Pageable.class
        );
        Method orphanAssets = SearchAdminController.class.getMethod(
            "orphanAssets",
            UUID.class,
            Pageable.class
        );

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

