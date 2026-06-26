package com.example.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    ResponseEntity<ApiError> handleAuth(AuthException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of(401, "Unauthorized", List.of(ex.getMessage())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiError.of(400, "Bad Request", List.of(ex.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(ApiError.of(400, "Validation Failed", details));
    }
}
