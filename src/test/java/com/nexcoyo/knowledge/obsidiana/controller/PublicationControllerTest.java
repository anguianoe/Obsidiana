package com.nexcoyo.knowledge.obsidiana.controller;

import com.nexcoyo.knowledge.obsidiana.dto.request.PublishPageRequest;
import com.nexcoyo.knowledge.obsidiana.dto.request.UserPublishPageRequest;
import com.nexcoyo.knowledge.obsidiana.facade.PublicationFacade;
import com.nexcoyo.knowledge.obsidiana.service.GeneralService;
import com.nexcoyo.knowledge.obsidiana.util.enums.PublicationStatus;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.assertj.core.api.Assertions.assertThat;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicationControllerTest {

    @Mock
    private PublicationFacade publicationFacade;
    @Mock
    private GeneralService generalService;

    @InjectMocks
    private PublicationController controller;

    @Test
    void liveSummariesDelegatesPageable() {
        Pageable pageable = Pageable.unpaged();

        controller.liveSummaries(pageable);

        verify(publicationFacade).liveSummaries(pageable);
    }

    @Test
    void publishUsesSessionUser() {
        UUID userId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(userId);
        UserPublishPageRequest request = new UserPublishPageRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "public-slug",
            "Public Title",
            "<p>content</p>",
            PublicationStatus.LIVE
        );

        controller.publish(request);

        verify(publicationFacade).publishForUser(request, userId);
    }

    @Test
    void publishAdminDelegatesWithoutOwnershipRestriction() {
        UUID actorId = UUID.randomUUID();
        when(generalService.getIdUserFromSession()).thenReturn(actorId);
        PublishPageRequest request = new PublishPageRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "public-slug",
            "Public Title",
            "<p>content</p>",
            PublicationStatus.LIVE,
            UUID.randomUUID()
        );

        controller.publishAdmin(request);

        ArgumentCaptor<PublishPageRequest> requestCaptor = ArgumentCaptor.forClass(PublishPageRequest.class);
        ArgumentCaptor<UUID> actorCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(publicationFacade).publish(requestCaptor.capture(), actorCaptor.capture());
        assertThat(requestCaptor.getValue()).isEqualTo(request);
        assertThat(actorCaptor.getValue()).isEqualTo(actorId);
    }

    @Test
    void publishEndpointsRequireExpectedRoles() throws NoSuchMethodException {
        Method publish = PublicationController.class.getMethod("publish", UserPublishPageRequest.class);
        Method publishAdmin = PublicationController.class.getMethod("publishAdmin", PublishPageRequest.class);

        PreAuthorize userAuth = publish.getAnnotation(PreAuthorize.class);
        PreAuthorize adminAuth = publishAdmin.getAnnotation(PreAuthorize.class);

        assertThat(userAuth).isNotNull();
        assertThat(userAuth.value()).isEqualTo("hasRole('USER')");
        assertThat(adminAuth).isNotNull();
        assertThat(adminAuth.value()).isEqualTo("hasRole('SUPER_ADMIN')");
    }

    @Test
    void liveSummariesHasDefaultPageSize50() throws NoSuchMethodException {
        Method method = PublicationController.class.getMethod("liveSummaries", Pageable.class);
        PageableDefault annotation = findPageableDefault(method);

        assertThat(annotation).isNotNull();
        assertThat(annotation.size()).isEqualTo(50);
    }

    private PageableDefault findPageableDefault(Method method) {
        for (Parameter parameter : method.getParameters()) {
            PageableDefault annotation = parameter.getAnnotation(PageableDefault.class);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }
}

