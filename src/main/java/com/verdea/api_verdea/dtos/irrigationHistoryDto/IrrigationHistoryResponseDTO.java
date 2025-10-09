package com.verdea.api_verdea.dtos.irrigationHistoryDto;

import com.verdea.api_verdea.dtos.plantDto.PlantSummary;

import java.time.LocalDateTime;

public record IrrigationHistoryResponseDTO(
        Long id,
        Double soilMoisture,
        String mode,
        Integer durationSeconds,
        LocalDateTime createdAt,
        PlantSummary plant,
        String deviceName
) {
}
