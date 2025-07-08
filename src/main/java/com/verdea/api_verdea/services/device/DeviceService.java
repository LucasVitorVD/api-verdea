package com.verdea.api_verdea.services.device;

import com.verdea.api_verdea.dtos.deviceDto.DeviceRequestDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceResponseDTO;
import com.verdea.api_verdea.entities.Device;
import com.verdea.api_verdea.repositories.DeviceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;

    @Transactional
    public DeviceResponseDTO registerDevice(DeviceRequestDTO dto) {
        if (deviceRepository.existsByMacAddress(dto.macAddress())) {
            throw new IllegalArgumentException("Dispositivo com esse MAC já está registrado");
        }

        // Geração automática de nome, se necessário
        String name = dto.name();

        if (name == null || name.isBlank()) {
            String macSuffix = dto.macAddress().replace(":", "").substring(6);
            name = "dispositivo-" + macSuffix;
        }

        Device device = Device.builder()
                .name(name)
                .macAddress(dto.macAddress())
                .status(dto.status())
                .createdAt(LocalDateTime.now())
                .build();

        Device savedDevice = deviceRepository.save(device);

        return new DeviceResponseDTO(savedDevice.getName(), savedDevice.getMacAddress(), savedDevice.getStatus());
    }
}
