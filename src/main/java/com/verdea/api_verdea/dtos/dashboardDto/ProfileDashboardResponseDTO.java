package com.verdea.api_verdea.dtos.dashboardDto;

public record ProfileDashboardResponseDTO(
        long totalPlants,
        long totalDevices,
        long totalIrrigationHistory,
        double engagementLevel
) {
}
