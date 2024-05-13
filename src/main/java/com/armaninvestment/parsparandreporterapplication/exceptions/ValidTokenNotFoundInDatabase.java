package com.armaninvestment.parsparandreporterapplication.exceptions;

public class ValidTokenNotFoundInDatabase extends RuntimeException {
    public ValidTokenNotFoundInDatabase(String message){
        super(message);
    }

    public ValidTokenNotFoundInDatabase(String message, Throwable cause){
        super(message, cause);
    }
}
