package com.verdea.api_verdea.exceptions;

public class DeviceAlreadyExistsException extends RuntimeException {
    public DeviceAlreadyExistsException() {}

    public DeviceAlreadyExistsException(String message) {
        super(message);
    }
}
