package org.elysia.api.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Handle ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(
                new ErrorResponse(ex.getClass().getSimpleName(), ex.getMessage(), HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationError(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage())
        );
        errors.put("status", HttpStatus.BAD_REQUEST.toString());
        errors.put("error", ex.getClass().getSimpleName());
        errors.put("message", ex.getMessage());
        errors.put("time", LocalDateTime.now().toString());
        return ResponseEntity.badRequest().body(errors);
    }

    // Handle Hibernate Validator triggered inside JPA (persist-time)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv ->
                errors.put(cv.getPropertyPath().toString(), cv.getMessage())
        );
        errors.put("status", HttpStatus.BAD_REQUEST.toString());
        errors.put("error", ex.getClass().getSimpleName());
        errors.put("message", ex.getMessage());
        errors.put("time", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return new ResponseEntity<>(
                new ErrorResponse(
                        ex.getClass().getSimpleName(),
                        "Data integrity violation: " + ex.getMostSpecificCause().getMessage(),
                        HttpStatus.BAD_REQUEST.value()
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    // Handle Generic Exceptions
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
//        return new ResponseEntity<>(
//                new ErrorResponse(ex.getClass().getSimpleName(),"An unexpected error occurred: " + ex.getMessage(),
//                        HttpStatus.INTERNAL_SERVER_ERROR.value()),
//                HttpStatus.INTERNAL_SERVER_ERROR
//        );
//    }
}
