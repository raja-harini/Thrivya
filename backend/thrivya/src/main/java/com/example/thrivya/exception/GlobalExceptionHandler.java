package com.example.thrivya.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles request DTO validation failures.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fieldErrors.putIfAbsent(
                                error.getField(),
                                defaultMessage(
                                        error.getDefaultMessage(),
                                        "Invalid value"
                                )
                        )
                );

        Map<String, Object> response = createErrorBody(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                request
        );

        response.put("errors", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Preserves the HTTP status explicitly chosen by a service.
     * Examples: 404 Not Found, 403 Forbidden, 409 Conflict.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>>
    handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        HttpStatusCode status = exception.getStatusCode();

        String message = defaultMessage(
                exception.getReason(),
                "Request could not be completed"
        );

        return ResponseEntity
                .status(status)
                .headers(exception.getHeaders())
                .body(createErrorBody(
                        status,
                        message,
                        request
                ));
    }

    /**
     * Handles invalid arguments passed to application services.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createErrorBody(
                        HttpStatus.BAD_REQUEST,
                        defaultMessage(
                                exception.getMessage(),
                                "Invalid request"
                        ),
                        request
                ));
    }

    /**
     * Handles uploads exceeding the configured multipart limit.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>>
    handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        HttpStatusCode status = HttpStatusCode.valueOf(413);

        return ResponseEntity
                .status(status)
                .body(createErrorBody(
                        status,
                        "Uploaded file exceeds the configured size limit.",
                        request
                ));
    }

    /**
     * Handles unexpected application-state errors.
     * Technical details are logged, not sent to the frontend.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException exception,
            HttpServletRequest request
    ) {
        log.error(
                "Application state error for {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorBody(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "The operation could not be completed. Please try again.",
                        request
                ));
    }

    /**
     * Fallback for unexpected exceptions not handled above.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error(
                "Unexpected error for {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorBody(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected server error occurred. Please try again.",
                        request
                ));
    }

    /**
     * Creates a consistent JSON error response.
     */
    private Map<String, Object> createErrorBody(
            HttpStatusCode status,
            String message,
            HttpServletRequest request
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("message", message);
        response.put("path", request.getRequestURI());

        return response;
    }

    private String defaultMessage(
            String message,
            String fallback
    ) {
        return message == null || message.isBlank()
                ? fallback
                : message;
    }
}