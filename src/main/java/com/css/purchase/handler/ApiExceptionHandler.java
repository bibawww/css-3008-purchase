package com.css.purchase.handler;

import com.css.purchase.domain.DuplicateRequest;
import com.css.purchase.domain.RequestNotFound;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .sorted()
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "validation failed";
        }
        return error(HttpStatus.BAD_REQUEST, "VALIDATION", message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> badPathValue(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = UUID.class.equals(ex.getRequiredType()) ? "id must be a UUID" : ex.getName() + " is invalid";
        return error(HttpStatus.BAD_REQUEST, "VALIDATION", message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> notJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION", "body must be JSON", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> unsupportedMedia(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED", "unsupported media type", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> unsupportedMethod(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "UNSUPPORTED", "method not supported", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> noResource(NoResourceFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "not found", request);
    }

    @ExceptionHandler(RequestNotFound.class)
    public ResponseEntity<ErrorResponse> notFound(RequestNotFound ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "purchase request not found", request);
    }

    @ExceptionHandler(DuplicateRequest.class)
    public ResponseEntity<ErrorResponse> duplicate(DuplicateRequest ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "DUPLICATE", "business key already exists", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpected(Exception ex, HttpServletRequest request) {
        String correlationId = correlationId(request);
        log.error("Unexpected error, correlationId={}", correlationId, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL", "unexpected error", correlationId));
    }

    private static ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message,
                                                       HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(code, message, correlationId(request)));
    }

    private static String correlationId(HttpServletRequest request) {
        String header = request.getHeader(CORRELATION_HEADER);
        return (header == null || header.isBlank()) ? UUID.randomUUID().toString() : header;
    }
}
