package com.verdea.api_verdea.dtos.plantDto;

import com.verdea.api_verdea.entities.Device;

import java.time.LocalDateTime;

public record PlantResponseDTO(
        Long id,
        String name,
        String species,
        String location,
        String notes,
        LocalDateTime wateringTime,
        Integer wateringFrequency,
        Double idealSoilMoisture,
        String imageUrl,
        Device device
) {
}
