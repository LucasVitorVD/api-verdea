package com.verdea.api_verdea.dtos.dashboardDto;

import java.time.Instant;

public record SoilMoistureChartDTO(
        Instant date,
        Double averageMoisture
) {
}
