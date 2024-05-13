package com.armaninvestment.parsparandreporterapplication.exceptions;

import java.time.LocalDateTime;

public record RestErrorResponse(int status, String message,
                                LocalDateTime timestamp) {}
