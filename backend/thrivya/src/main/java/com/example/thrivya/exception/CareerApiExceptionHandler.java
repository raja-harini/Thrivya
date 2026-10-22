package com.example.thrivya.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CareerApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(
            ResponseStatusException exception
    ) {
        String message =
                exception.getReason() == null
                        ? "Request could not be completed"
                        : exception.getReason();

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(
                        Map.of(
                                "status",
                                exception.getStatusCode().value(),
                                "message",
                                message
                        )
                );
    }
}