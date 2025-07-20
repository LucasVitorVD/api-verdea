package com.verdea.api_verdea.exceptions;

public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException() {}

    public DeviceNotFoundException(String message) {
        super(message);
    }
}
