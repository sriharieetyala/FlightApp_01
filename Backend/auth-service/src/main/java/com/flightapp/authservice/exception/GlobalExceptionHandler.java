package com.flightapp.authservice.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles our custom errors (existing username, bad login)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        Map<String, Object> body = new HashMap<>();
       // body.put("status", "BAD_REQUEST");
        body.put("message", ex.getReason());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Handles missing/blank fields validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldError().getDefaultMessage();

        Map<String, Object> body = new HashMap<>();
       // body.put("status", "BAD_REQUEST");
        body.put("message", errorMsg);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
