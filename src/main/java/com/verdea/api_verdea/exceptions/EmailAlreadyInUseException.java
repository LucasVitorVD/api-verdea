package com.verdea.api_verdea.exceptions;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException() {}

    public EmailAlreadyInUseException(String message) {
        super(message);
    }
}
