package com.nexcoyo.knowledge.obsidiana.common.web;

import com.nexcoyo.knowledge.obsidiana.common.dto.ApiErrorResponse;
import com.nexcoyo.knowledge.obsidiana.common.exception.ApiException;
import com.nexcoyo.knowledge.obsidiana.common.exception.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity< ApiErrorResponse > handleNotFound( EntityNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Resource not found", request, Collections.emptyList());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, ex.getMessage(), request, Collections.emptyList());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        return build(ex.status(), ex.code(), ex.getMessage(), request, ex.details());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedState(IllegalStateException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, ex.getMessage(), request, Collections.emptyList());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .toList();
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Validation failed", request, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        List<String> details = ex.getConstraintViolations().stream().map(v -> v.getMessage()).toList();
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Constraint violation", request, details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "An unexpected error occurred", request, Collections.emptyList());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, ErrorCode code, String message, HttpServletRequest request, List<String> details) {
        return ResponseEntity.status(status).body(
            new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                code.name(),
                message,
                request.getRequestURI(),
                details
            )
        );
    }
}
