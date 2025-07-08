package com.verdea.api_verdea.enums;

public enum DeviceStatus {
    ONLINE("ONLINE"),
    OFFLINE("OFFLINE");

    private final String text;

    DeviceStatus(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}