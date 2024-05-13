package com.armaninvestment.parsparandreporterapplication.exceptions;

public class CustomAuthenticationException extends RuntimeException {

    public CustomAuthenticationException(String message) {
        super(message);
    }

    // Constructor that accepts a message and a cause
    public CustomAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

