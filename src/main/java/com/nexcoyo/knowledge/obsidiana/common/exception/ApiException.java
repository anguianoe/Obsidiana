package com.nexcoyo.knowledge.obsidiana.common.exception;

import java.util.List;
import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode code;
    private final List<String> details;

    public ApiException(HttpStatus status, ErrorCode code, String message) {
        this(status, code, message, List.of());
    }

    public ApiException(HttpStatus status, ErrorCode code, String message, List<String> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details;
    }

    public HttpStatus status() {
        return status;
    }

    public ErrorCode code() {
        return code;
    }

    public List<String> details() {
        return details;
    }
}

