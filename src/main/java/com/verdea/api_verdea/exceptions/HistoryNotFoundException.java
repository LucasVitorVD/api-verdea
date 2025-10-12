package com.verdea.api_verdea.exceptions;

public class HistoryNotFoundException extends RuntimeException {
    public HistoryNotFoundException(String message) {
        super(message);
    }

    public HistoryNotFoundException() {}
}
