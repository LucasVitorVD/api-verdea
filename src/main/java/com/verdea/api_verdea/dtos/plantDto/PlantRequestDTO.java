package com.verdea.api_verdea.dtos.plantDto;

import com.verdea.api_verdea.enums.WateringFrequency;

public record PlantRequestDTO(
        String name,
        String species,
        String location,
        String notes,
        String wateringTime,
        WateringFrequency wateringFrequency,
        Double idealSoilMoisture,
        String imageUrl,
        String deviceMacAddress
        ) {
}
