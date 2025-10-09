package com.verdea.api_verdea.services.irrigationHistory;

import com.verdea.api_verdea.dtos.irrigationHistoryDto.IrrigationHistoryRequestDTO;
import com.verdea.api_verdea.dtos.irrigationHistoryDto.IrrigationHistoryResponseDTO;
import com.verdea.api_verdea.dtos.plantDto.PlantSummary;
import com.verdea.api_verdea.entities.Device;
import com.verdea.api_verdea.entities.IrrigationHistory;
import com.verdea.api_verdea.entities.Plant;
import com.verdea.api_verdea.entities.User;
import com.verdea.api_verdea.exceptions.DeviceNotFoundException;
import com.verdea.api_verdea.exceptions.PlantNotFoundException;
import com.verdea.api_verdea.exceptions.UserNotFoundException;
import com.verdea.api_verdea.repositories.DeviceRepository;
import com.verdea.api_verdea.repositories.IrrigationHistoryRepository;
import com.verdea.api_verdea.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IrrigationHistoryService {
    private final IrrigationHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    @Transactional
    public void saveIrrigation(IrrigationHistoryRequestDTO dto) {
        Device device = deviceRepository.findByMacAddress(dto.deviceMacAddress())
                .orElseThrow(() -> new DeviceNotFoundException("Dispositivo não encontrado"));

        Plant plant = device.getPlant();

        if (plant == null) {
            throw new PlantNotFoundException("Dispositivo não está vinculado a nenhuma planta");
        }

        IrrigationHistory history = IrrigationHistory.builder()
                .device(device)
                .plant(plant)
                .soilMoisture(dto.soilMoisture())
                .mode(dto.mode())
                .durationSeconds(dto.durationSeconds())
                .build();

        historyRepository.save(history);
    }

    public Page<IrrigationHistoryResponseDTO> getHistoryByUserId(Pageable pageable, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        Page<IrrigationHistory> irrigationHistoryPage = historyRepository.findAllByUserId(user.getId(), pageable);

        return irrigationHistoryPage.map(this::mapToResponseDTO);
    }

    private IrrigationHistoryResponseDTO mapToResponseDTO(IrrigationHistory history) {
        PlantSummary plantSummary = new PlantSummary(
                history.getPlant().getId(),
                history.getPlant().getName(),
                history.getPlant().getSpecies(),
                history.getPlant().getImageUrl()
        );

        return new IrrigationHistoryResponseDTO(
                history.getId(),
                history.getSoilMoisture(),
                String.valueOf(history.getMode()),
                history.getDurationSeconds(),
                history.getCreatedAt(),
                plantSummary,
                history.getDevice().getName()
        );
    }
}
