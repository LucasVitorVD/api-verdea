package com.verdea.api_verdea.exceptions;

public class PlantNotFoundException extends RuntimeException {
    public PlantNotFoundException() {}
    public PlantNotFoundException(String message) {
        super(message);
    }
}
