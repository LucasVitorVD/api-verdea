package com.verdea.api_verdea.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum WateringFrequency {
    ONCE_A_DAY("once_a_day"),
    EVERY_TWO_DAYS("every_2_days"),
    WEEKLY("weekly"),
    TWICE_A_DAY("twice_a_day");

    private final String value;

    WateringFrequency(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}