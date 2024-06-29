package com.armaninvestment.parsparandreporterapplication.exceptions;

import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestErrorResponse handleTypeMismatchException(HttpServletRequest request, final MethodArgumentTypeMismatchException ex) {
        logger.error("requestURI: {}, errorMessage: {}", request.getRequestURI(), ex.getMessage());
        return new RestErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                getTypeMismatchErrorMessage(ex),
                LocalDateTime.now());
    }

    private String getTypeMismatchErrorMessage(MethodArgumentTypeMismatchException ex) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        if (StringUtils.hasText(ex.getName())) {
            errorMessageBuilder.append(ex.getName());
        } else {
            errorMessageBuilder.append("Argument");
        }
        errorMessageBuilder.append(String.format(" [%s] نوع نادرست است. نوع صحیح %s است.",
                ex.getValue(),
                ex.getRequiredType()));

        return errorMessageBuilder.toString();
    }
}
