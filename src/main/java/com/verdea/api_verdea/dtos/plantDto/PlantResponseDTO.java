package com.verdea.api_verdea.dtos.plantDto;

import java.time.LocalDateTime;

public record PlantResponseDTO(
        String name,
        String species,
        String location,
        String notes,
        LocalDateTime wateringTime,
        Integer wateringFrequency,
        Double idealSoilMoisture,
        String imageUrl,
        String deviceMacAddress
) {
}
