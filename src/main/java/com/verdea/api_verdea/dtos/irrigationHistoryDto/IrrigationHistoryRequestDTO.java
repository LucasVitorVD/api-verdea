package com.verdea.api_verdea.dtos.irrigationHistoryDto;

import com.verdea.api_verdea.enums.WateringMode;

public record IrrigationHistoryRequestDTO (
        Double soilMoisture,
        WateringMode mode,
        Integer durationSeconds,
        String deviceMacAddress
) {}
