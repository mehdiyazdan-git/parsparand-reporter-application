package com.armaninvestment.parsparandreporterapplication.exceptions;

import java.io.Serial;

public class CustomMissingClaimException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public CustomMissingClaimException(String message) {
        super(message);
    }
}