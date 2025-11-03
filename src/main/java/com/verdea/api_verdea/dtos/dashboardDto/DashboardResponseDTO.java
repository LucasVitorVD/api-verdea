package com.verdea.api_verdea.dtos.dashboardDto;

import com.verdea.api_verdea.dtos.plantDto.LastIrrigationDTO;

public record DashboardResponseDTO(
        long totalPlants,
        long totalDevices,
        long onlineDevices,
        long offlineDevices,
        LastIrrigationDTO lastIrrigation,
        Double averageSoilMoisture
) {
}
