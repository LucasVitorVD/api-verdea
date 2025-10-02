package com.verdea.api_verdea.dtos.plantDto;

import com.verdea.api_verdea.enums.WateringFrequency;
import com.verdea.api_verdea.enums.WateringMode;

public record PlantRequestDTO(
        String name,
        String species,
        String location,
        String notes,
        String wateringTime,
        WateringFrequency wateringFrequency,
        Double idealSoilMoisture,
        WateringMode mode,
        String imageUrl,
        String deviceMacAddress
        ) {
}
