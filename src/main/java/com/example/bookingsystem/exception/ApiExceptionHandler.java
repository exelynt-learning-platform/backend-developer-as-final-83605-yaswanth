package com.example.bookingsystem.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(
            NotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error(ex.getMessage()));
    }

    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<Map<String, Object>> bookingConflict(
            BookingConflictException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(
            IllegalArgumentException ex) {

        return ResponseEntity
                .badRequest()
                .body(error(ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> invalidLogin(
            BadCredentialsException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error("Invalid username or password"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(
            MethodArgumentNotValidException ex) {

        Map<String, Object> fields = new LinkedHashMap<>();

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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> typeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String message =
                "Invalid value for parameter '"
                        + ex.getName()
                        + "'";

        return ResponseEntity
                .badRequest()
                .body(error(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> messageNotReadable(
            HttpMessageNotReadableException ex) {

        return ResponseEntity
                .badRequest()
                .body(error("Request body is invalid or malformed"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> dataIntegrityViolation(
            DataIntegrityViolationException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error(
                        "The request violates a database constraint"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> methodNotSupported(
            HttpRequestMethodNotSupportedException ex) {

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(error(
                        "HTTP method is not supported for this endpoint"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> missingParameter(
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