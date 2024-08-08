package com.armaninvestment.parsparandreporterapplication.exceptions;

import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DatabaseIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    RestErrorResponse handleDatabaseIntegrityViolationException(
            DatabaseIntegrityViolationException ex) {
        logger.error("Error in method handleDatabaseIntegrityViolationException: ", ex);
        return new RestErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    RestErrorResponse handleEntityNotFoundException(
            EntityNotFoundException ex) {
        logger.error("Error in method handleEntityNotFoundException: ", ex);
        return new RestErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    RestErrorResponse handleIllegalArgumentException(
            IllegalArgumentException ex) {
        logger.error("Error in method handleIllegalArgumentException: ", ex);
        return new RestErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    RestErrorResponse handleException(Exception ex) {
        logger.error("Error in method handleException: ", ex);
        return new RestErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestErrorResponse handleValidationExceptions(Exception ex) {
        logger.error("Error in method handleValidationExceptions: ", ex);

        String errorMessage = "Validation failed:";
        if (ex instanceof MethodArgumentNotValidException) {
            errorMessage += ((MethodArgumentNotValidException) ex).getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
        } else if (ex instanceof BindException) {
            errorMessage += ((BindException) ex).getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
        }

        return new RestErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                errorMessage,
                LocalDateTime.now());
    }

    @ExceptionHandler(TypeMismatchException.class) // Specific for TypeMismatchException
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestErrorResponse handleTypeMismatchException(HttpServletRequest request, final TypeMismatchException ex) {
        logger.error("requestURI: {}, errorMessage: {}", request.getRequestURI(), ex.getMessage());
        return new RestErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Type mismatch: " + ex.getMessage(),  // More general error message
                LocalDateTime.now());
    }
}
