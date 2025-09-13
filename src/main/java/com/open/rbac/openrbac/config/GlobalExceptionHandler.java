package com.open.rbac.openrbac.config;

import com.open.rbac.openrbac.responses.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Helper method to build a consistent ErrorResponse.
     */
    private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status, WebRequest request,
                                                        Map<String, String> errors) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(message)
                .status(status.value())
                .timestamp(new Date())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .errors(errors)
                .build();
        return ResponseEntity.status(status).body(errorResponse);
    }

    // ==================== Validation Exceptions ====================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
                                                                    WebRequest request) {

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> {
                            // Check if this error is a type mismatch related to date conversion
                            if ("typeMismatch".equals(fieldError.getCode())) {
                                return "Invalid format";
                            }
                            return fieldError.getDefaultMessage(); // fallback to default message
                        },
                        (existing, replacement) -> existing // handle duplicate keys if any
                ));

        log.warn("Validation error: {}", errors);
        return buildResponse("Validation failed", HttpStatus.BAD_REQUEST, request, errors);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex,
                                                                            WebRequest request) {
        Map<String, String> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), v -> v.getMessage()));

        log.warn("Constraint violation: {}", errors);
        return buildResponse("Constraint violation", HttpStatus.BAD_REQUEST, request, errors);
    }

    // ==================== Entity / Resource Not Found ====================
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        log.warn("Entity not found: {}", ex.getMessage());
        return buildResponse(Optional.ofNullable(ex.getMessage()).orElse("Entity not found"), HttpStatus.NOT_FOUND,
                request, null);
    }

    // ==================== Illegal Argument / State ====================
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return buildResponse(Optional.ofNullable(ex.getMessage()).orElse("Invalid argument"), HttpStatus.BAD_REQUEST,
                request, null);
    }

    @ExceptionHandler({IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, WebRequest request) {
        log.warn("Illegal state: {}", ex.getMessage());
        return buildResponse(Optional.ofNullable(ex.getMessage()).orElse("Invalid state"), HttpStatus.CONFLICT, request,
                null);
    }

    // ==================== Data Integrity ====================
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                      WebRequest request) {
        String message = "Data integrity violation";

        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("duplicate key")) {
                message = "Duplicate entry - record already exists";
            } else if (ex.getMessage().contains("foreign key")) {
                message = "Cannot delete - record is referenced by other data";
            } else if (ex.getMessage().contains("not null")) {
                message = "Required field is missing";
            }
        }

        log.warn("Data integrity violation: {}", ex.getMessage());
        return buildResponse(message, HttpStatus.CONFLICT, request, null);
    }

    // ==================== Request / Type Exceptions ====================
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            WebRequest request) {
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());

        log.warn("Method argument type mismatch: {}", message);
        return buildResponse(message, HttpStatus.BAD_REQUEST, request, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex, WebRequest request) {
        String message = "Malformed JSON request";
        if (ex.getMessage() != null && ex.getMessage().contains("JSON parse error")) {
            message = "Invalid JSON format in request body";
        }
        log.warn("Malformed JSON: {}", ex.getMessage());
        return buildResponse(message, HttpStatus.BAD_REQUEST, request, null);
    }

    // ==================== Security Exceptions ====================
    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class, SecurityException.class})
    public ResponseEntity<ErrorResponse> handleSecurityExceptions(Exception ex, WebRequest request) {
        log.warn("Security exception: {}", ex.getMessage());
        return buildResponse("Access denied", HttpStatus.FORBIDDEN, request, null);
    }

    // ==================== Runtime / Catch-all ====================

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, WebRequest request) {
        log.warn("No resource found: {}", ex.getMessage());
        return buildResponse(
                "API endpoint not found",
                HttpStatus.NOT_FOUND,
                request,
                null);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        return buildResponse(Optional.ofNullable(ex.getMessage()).orElse("An unexpected error occurred"),
                HttpStatus.INTERNAL_SERVER_ERROR, request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);
        return buildResponse("An internal server error occurred", HttpStatus.INTERNAL_SERVER_ERROR, request, null);
    }
}