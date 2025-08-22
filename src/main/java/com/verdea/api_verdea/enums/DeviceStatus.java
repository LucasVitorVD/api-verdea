package com.verdea.api_verdea.enums;

public enum DeviceStatus {
    ONLINE("online"),
    OFFLINE("offline");

    private final String text;

    DeviceStatus(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}