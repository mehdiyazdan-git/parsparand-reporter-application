package com.armaninvestment.parsparandreporterapplication.exceptions;

public class DuplicateNationalIdException extends RuntimeException {

    public DuplicateNationalIdException(String message) {
        super(message);
    }

    public DuplicateNationalIdException() {
        super("کد ملی تکراری است."); // Default message in Persian
    }
}

