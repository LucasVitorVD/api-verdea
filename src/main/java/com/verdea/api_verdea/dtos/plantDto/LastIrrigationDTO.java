package com.verdea.api_verdea.dtos.plantDto;

import java.time.Instant;

public record LastIrrigationDTO(
        String plantName,
        Instant date,
        double soilMoisture
) {
}
