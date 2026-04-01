package com.example.cp.hub.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles all RuntimeExceptions thrown from services (duplicate email, wrong password, etc.)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", ex.getMessage(),
                        "status", 400,
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    // Handles any unexpected exception as a generic 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Something went wrong",
                        "status", 500,
                        "timestamp", LocalDateTime.now().toString()
                ));
    }
}
