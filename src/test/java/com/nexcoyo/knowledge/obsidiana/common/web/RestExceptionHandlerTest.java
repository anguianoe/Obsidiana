package com.nexcoyo.knowledge.obsidiana.common.web;

import com.nexcoyo.knowledge.obsidiana.common.dto.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void illegalStateMapsToUnauthorized() {
        var request = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/page-revisions/latest/123");

        var response = handler.handleUnauthorizedState(new IllegalStateException("Session expired"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        ApiErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.code()).isEqualTo("UNAUTHORIZED");
        assertThat(body.message()).isEqualTo("Session expired");
    }
}

