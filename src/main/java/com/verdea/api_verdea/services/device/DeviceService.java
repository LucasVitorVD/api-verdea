package com.verdea.api_verdea.services.device;

import com.verdea.api_verdea.dtos.deviceDto.DeviceAssignmentResponseDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceRequestDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceResponseDTO;
import com.verdea.api_verdea.entities.Device;
import com.verdea.api_verdea.entities.User;
import com.verdea.api_verdea.exceptions.DeviceAlreadyAssignedException;
import com.verdea.api_verdea.exceptions.DeviceAlreadyExistsException;
import com.verdea.api_verdea.exceptions.DeviceNotFoundException;
import com.verdea.api_verdea.exceptions.UserNotFoundException;
import com.verdea.api_verdea.repositories.DeviceRepository;
import com.verdea.api_verdea.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public DeviceResponseDTO registerDevice(DeviceRequestDTO dto) {
        if (deviceRepository.existsByMacAddress(dto.macAddress())) {
            throw new DeviceAlreadyExistsException("Dispositivo com esse MAC já está registrado");
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
                .createdAt(LocalDateTime.now())
                .build();

        Device savedDevice = deviceRepository.save(device);

        return new DeviceResponseDTO(savedDevice.getName(), savedDevice.getMacAddress(), savedDevice.getCreatedAt());
    }

    @Transactional
    public DeviceAssignmentResponseDTO assignDeviceToUser(String macAddress, String userEmail) {
        Device device = deviceRepository.findByMacAddress(macAddress).orElseThrow(() -> new DeviceNotFoundException("Dispositivo com MAC " + macAddress + " não encontrado"));

        if (device.getUser() != null) {
            throw new DeviceAlreadyAssignedException("Dispositivo já vinculado a um usuário");
        }

        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        device.setUser(user);
        deviceRepository.save(device);

        return new DeviceAssignmentResponseDTO(device.getName(), device.getMacAddress(), device.getUser().getEmail(), LocalDateTime.now());
    }
}
