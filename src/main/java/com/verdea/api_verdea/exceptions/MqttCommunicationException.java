package com.verdea.api_verdea.exceptions;

public class MqttCommunicationException extends RuntimeException {
    public MqttCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
