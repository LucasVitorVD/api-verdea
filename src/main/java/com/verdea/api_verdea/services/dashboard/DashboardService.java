package com.verdea.api_verdea.services.dashboard;

import com.verdea.api_verdea.dtos.dashboardDto.DashboardResponseDTO;
import com.verdea.api_verdea.dtos.dashboardDto.SoilMoistureChartDTO;
import com.verdea.api_verdea.dtos.plantDto.LastIrrigationDTO;
import com.verdea.api_verdea.enums.DeviceStatus;
import com.verdea.api_verdea.exceptions.UserNotFoundException;
import com.verdea.api_verdea.repositories.DeviceRepository;
import com.verdea.api_verdea.repositories.IrrigationHistoryRepository;
import com.verdea.api_verdea.repositories.PlantRepository;
import com.verdea.api_verdea.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class DashboardService {
    private final UserRepository userRepository;
    private final PlantRepository plantRepository;
    private final DeviceRepository deviceRepository;
    private final IrrigationHistoryRepository irrigationHistoryRepository;

    @Autowired
    public DashboardService(
            UserRepository userRepository,
            PlantRepository plantRepository,
            DeviceRepository deviceRepository,
            IrrigationHistoryRepository irrigationHistoryRepository
    ) {
        this.userRepository = userRepository;
        this.plantRepository = plantRepository;
        this.deviceRepository = deviceRepository;
        this.irrigationHistoryRepository = irrigationHistoryRepository;
    }

    public DashboardResponseDTO getDashboardData(String userEmail) {
        long userId = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException("Usuário não encontrado")).getId();

        long totalPlants = plantRepository.countByUserId(userId);
        long totalDevices = deviceRepository.countByUserId(userId);
        long onlineDevices = deviceRepository.countByUserIdAndStatus(userId, DeviceStatus.ONLINE);
        long offlineDevices = deviceRepository.countByUserIdAndStatus(userId, DeviceStatus.OFFLINE);

        LastIrrigationDTO lastIrrigation = irrigationHistoryRepository.findTopByPlant_UserIdOrderByCreatedAtDesc(userId)
                .map(history -> new LastIrrigationDTO(
                        history.getPlant().getName(),
                        history.getCreatedAt(),
                        history.getSoilMoisture()))
                .orElse(null);

        double avgMoisture = irrigationHistoryRepository.findAverageSoilMoistureByUserId(userId).orElse(0.0);

        return new DashboardResponseDTO(
                totalPlants,
                totalDevices,
                onlineDevices,
                offlineDevices,
                lastIrrigation,
                avgMoisture
        );
    }

    public List<SoilMoistureChartDTO> getSoilMoistureData(String userEmail) {
        long userId = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado")).getId();

        return irrigationHistoryRepository.findSoilMoistureDataByUserId(userId)
                .stream()
                .map(result -> new SoilMoistureChartDTO(
                        (Instant) result[0],
                        (Double) result[1]
                ))
                .toList();
    }
}
