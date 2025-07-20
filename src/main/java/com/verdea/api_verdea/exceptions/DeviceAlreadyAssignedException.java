package com.verdea.api_verdea.exceptions;

public class DeviceAlreadyAssignedException extends RuntimeException {
    public DeviceAlreadyAssignedException() {}
    public DeviceAlreadyAssignedException(String message) {
        super(message);
    }
}
