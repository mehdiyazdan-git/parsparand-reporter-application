package com.armaninvestment.parsparandreporterapplication.exceptions;

import java.io.Serial;

public class DuplicateEntityException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    public DuplicateEntityException(String message) {

        super(message);
    }
}
