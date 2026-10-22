package com.ecommerce.exception;

import com.ecommerce.dto.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;


@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse>
    handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request) {

        ApiErrorResponse response =
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.NOT_FOUND.value(),
                        "Not Found",
                        exception.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse>
    handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        ApiErrorResponse response =
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        exception.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .badRequest()
                .body(response);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse>
    handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        String message =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .findFirst()
                        .map(error ->
                                error.getField()
                                        + ": "
                                        + error.getDefaultMessage()
                        )
                        .orElse(
                                "Validation failed"
                        );

        ApiErrorResponse response =
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation Failed",
                        message,
                        request.getRequestURI()
                );

        return ResponseEntity
                .badRequest()
                .body(response);
    }
}