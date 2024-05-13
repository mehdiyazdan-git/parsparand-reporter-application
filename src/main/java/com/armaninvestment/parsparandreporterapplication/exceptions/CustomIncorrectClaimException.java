package com.armaninvestment.parsparandreporterapplication.exceptions;

import java.io.Serial;

public class CustomIncorrectClaimException extends Exception{

    @Serial
    private static final long serialVersionUID = 1L;

    public CustomIncorrectClaimException(String message) {
        super(message);
    }
}
