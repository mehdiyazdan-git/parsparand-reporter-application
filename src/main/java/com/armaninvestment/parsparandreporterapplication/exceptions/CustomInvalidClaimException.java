package com.armaninvestment.parsparandreporterapplication.exceptions;

import java.io.Serial;

public class CustomInvalidClaimException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public CustomInvalidClaimException(String message){

        super(message);
    }
}
