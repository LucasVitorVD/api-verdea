package com.verdea.api_verdea.services.device;

import com.verdea.api_verdea.dtos.deviceDto.DeviceAssignmentResponseDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceRequestDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceResponseDTO;
import com.verdea.api_verdea.dtos.plantDto.PlantResponseDTO;
import com.verdea.api_verdea.dtos.plantDto.PlantSummary;
import com.verdea.api_verdea.entities.Device;
import com.verdea.api_verdea.entities.Plant;
import com.verdea.api_verdea.entities.User;
import com.verdea.api_verdea.exceptions.*;
import com.verdea.api_verdea.repositories.DeviceRepository;
import com.verdea.api_verdea.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

        return mapToDeviceResponseDTO(savedDevice);
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

    public List<DeviceResponseDTO> getDevicesByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        List<Device> devices = deviceRepository.findAllByUser(user);

        return devices.stream()
                .map(this::mapToDeviceResponseDTO)
                .toList();
    }

    @Transactional
    public DeviceResponseDTO getDeviceById(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Dispositivo não encontrado."));

        return mapToDeviceResponseDTO(device);
    }

    @Transactional
    public void deleteDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Dispositivo não encontrado."));

        deviceRepository.delete(device);
    }

    // Método privado para mapear Device para DeviceResponseDTO
    private DeviceResponseDTO mapToDeviceResponseDTO(Device device) {
        PlantSummary plantSummary = null;

        if (device.getPlant() != null) {
            plantSummary = new PlantSummary(
                    device.getPlant().getId(),
                    device.getPlant().getName(),
                    device.getPlant().getSpecies(),
                    device.getPlant().getImageUrl()
            );
        }

        return new DeviceResponseDTO(
                device.getId(),
                device.getName(),
                device.getMacAddress(),
                device.getCreatedAt(),
                plantSummary
        );
    }
}
