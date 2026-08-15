package com.ams.exception;

import com.ams.dto.FieldErrorDTO;
import com.ams.dto.ValidationErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<FieldErrorDTO> fieldErrors = new ArrayList<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = (error instanceof FieldError) ? ((FieldError) error).getField() : error.getObjectName();
            Object rejectedValue = (error instanceof FieldError) ? ((FieldError) error).getRejectedValue() : null;
            String errorMessage = error.getDefaultMessage();

            fieldErrors.add(new FieldErrorDTO(fieldName, errorMessage, rejectedValue));
        });

        ValidationErrorResponseDTO response = ValidationErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failure")
                .message("Input validation constraints failed.")
                .fieldErrors(fieldErrors)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ValidationErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        ValidationErrorResponseDTO response = ValidationErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .fieldErrors(List.of(new FieldErrorDTO("global", ex.getMessage(), null)))
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationErrorResponseDTO> handleGeneralException(Exception ex) {
        ValidationErrorResponseDTO response = ValidationErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Server Error")
                .message(ex.getMessage() != null ? ex.getMessage() : "An unexpected database or system exception occurred.")
                .fieldErrors(List.of(new FieldErrorDTO("system", ex.getLocalizedMessage(), null)))
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
