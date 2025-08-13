package com.verdea.api_verdea.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum WateringFrequency {
    EVERY_DAY("once_a_day"),
    EVERY_TWO_DAYS("every_2_days"),
    EVERY_WEEK("weekly"),
    TWICE_DAILY("twice_a_day");

    private final String value;

    WateringFrequency(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}