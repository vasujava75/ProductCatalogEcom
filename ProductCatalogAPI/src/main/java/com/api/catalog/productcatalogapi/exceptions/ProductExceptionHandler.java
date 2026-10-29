package com.api.catalog.productcatalogapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ProductExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e ->
                errors.put(e.getField(), e.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonParse(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();
        String message = "Invalid JSON";
        if (cause != null && cause.getMessage() != null) {
            message += ": " + cause.getMessage();
        } else {
            message += ": " + ex.getMessage();
        }
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleProductNotFoundException(ProductNotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(DuplicateSkuException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDuplicateSkuException(DuplicateSkuException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleOptimisticLocking(ObjectOptimisticLockingFailureException ex) {
        return Map.of("error", "Product was modified concurrently; please refetch and retry");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAll(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        return ResponseEntity.internalServerError().body(error);
    }
}
