package com.verdea.api_verdea.dtos.irrigationHistoryDto;

import com.verdea.api_verdea.dtos.plantDto.PlantSummary;

import java.time.Instant;

public record IrrigationHistoryResponseDTO(
        Long id,
        Double soilMoisture,
        String mode,
        Integer durationSeconds,
        Instant createdAt,
        PlantSummary plant,
        String deviceName
) {
}
