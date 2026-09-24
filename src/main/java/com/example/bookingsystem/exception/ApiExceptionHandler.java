package com.example.bookingsystem.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> notFound(
            NotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error(ex.getMessage()));
    }

    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<?> bookingConflict(
            BookingConflictException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(
            IllegalArgumentException ex) {

        return ResponseEntity
                .badRequest()
                .body(error(ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> invalidLogin(
            BadCredentialsException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error("Invalid username or password"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fields =
                new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(field ->
                        fields.put(
                                field.getField(),
                                field.getDefaultMessage()));

        return ResponseEntity
                .badRequest()
                .body(fields);
    }

    /*
     * Handles invalid path/query parameter types.
     *
     * Example:
     * GET /api/assets?page=abc
     *
     * "abc" cannot be converted to int.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> typeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String message =
                "Invalid value for parameter '"
                        + ex.getName()
                        + "'";

        return ResponseEntity
                .badRequest()
                .body(error(message));
    }

    /*
     * Handles malformed JSON request bodies and invalid enum values.
     *
     * Example:
     * {
     *   "state": "INVALID"
     * }
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> messageNotReadable(
            HttpMessageNotReadableException ex) {

        return ResponseEntity
                .badRequest()
                .body(error(
                        "Request body is invalid or malformed"));
    }

    /*
     * Handles database constraint violations.
     *
     * Examples:
     * - Duplicate username
     * - Duplicate email
     * - Other unique/foreign-key constraints
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> dataIntegrityViolation(
            DataIntegrityViolationException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error(
                        "The request violates a database constraint"));
    }

    /*
     * Handles requests using an unsupported HTTP method.
     *
     * Example:
     * DELETE on an endpoint that only supports GET.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> methodNotSupported(
            HttpRequestMethodNotSupportedException ex) {

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(error(
                        "HTTP method is not supported for this endpoint"));
    }

    /*
     * Handles missing required request parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> missingParameter(
            MissingServletRequestParameterException ex) {

        return ResponseEntity
                .badRequest()
                .body(error(
                        "Required parameter '"
                                + ex.getParameterName()
                                + "' is missing"));
    }

    private Map<String, Object> error(String message) {

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("message", message);

        return body;
    }
}