package com.verdea.api_verdea.services.admin;

import com.verdea.api_verdea.dtos.deviceDto.DeviceAssignmentResponseDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceAvailableResponseDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceRequestDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceResponseDTO;
import com.verdea.api_verdea.dtos.userDto.UserResponseDTO;
import com.verdea.api_verdea.entities.Device;
import com.verdea.api_verdea.entities.User;
import com.verdea.api_verdea.enums.DeviceStatus;
import com.verdea.api_verdea.exceptions.DeviceAlreadyAssignedException;
import com.verdea.api_verdea.exceptions.DeviceAlreadyExistsException;
import com.verdea.api_verdea.exceptions.DeviceNotFoundException;
import com.verdea.api_verdea.exceptions.UserNotFoundException;
import com.verdea.api_verdea.repositories.DeviceRepository;
import com.verdea.api_verdea.repositories.UserRepository;
import com.verdea.api_verdea.services.mqtt.MqttService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDeviceService {
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final MqttService mqttService;

    public List<DeviceResponseDTO> getAllDevices() {
        return deviceRepository.findAll().stream()
                .map(this::mapToDeviceResponseDTO)
                .toList();
    }

    public List<DeviceAvailableResponseDTO> getDevicesWithUser() {
        return deviceRepository.findAllByUserIsNotNull()
                .stream()
                .map(this::mapToDeviceAvailableResponseDTO)
                .toList();
    }

    public DeviceResponseDTO getDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Dispositivo não encontrado."));
        return mapToDeviceResponseDTO(device);
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

    @Transactional
    public DeviceResponseDTO updateDevice(Long id, DeviceRequestDTO dto) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Dispositivo não encontrado."));

        device.setName(dto.name());

        if (dto.macAddress() != null && !dto.macAddress().isBlank() &&
                !dto.macAddress().equals(device.getMacAddress())) {

            if (deviceRepository.existsByMacAddress(dto.macAddress())) {
                throw new DeviceAlreadyExistsException("Outro dispositivo com esse MAC já existe");
            }
            device.setMacAddress(dto.macAddress());
        }

        if (dto.currentIp() != null && !dto.currentIp().isBlank()) {
            device.setCurrentIp(dto.currentIp());
        }

        Device updatedDevice = deviceRepository.save(device);
        return mapToDeviceResponseDTO(updatedDevice);
    }

    @Transactional
    public void deleteDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Dispositivo não encontrado."));
        deviceRepository.delete(device);
    }

    public void resetWifi(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Dispositivo não encontrado."));

        String topic = "verdea/commands/" + device.getMacAddress().replace(":", "");
        mqttService.publish(topic, "RESET_WIFI");
    }

    private DeviceResponseDTO mapToDeviceResponseDTO(Device device) {
        boolean isOnline = device.getStatus() == DeviceStatus.ONLINE;
        return new DeviceResponseDTO(
                device.getId(),
                device.getName(),
                device.getMacAddress(),
                device.getCurrentIp(),
                device.getCreatedAt(),
                isOnline,
                null // não associamos planta aqui
        );
    }

    private DeviceAvailableResponseDTO mapToDeviceAvailableResponseDTO(Device device) {
        User user = device.getUser();

        UserResponseDTO userResponseDTO = new UserResponseDTO(user.getId(), user.getEmail(), user.getRole(), user.getCreatedAt());

        return new DeviceAvailableResponseDTO(
                device.getId(),
                device.getName(),
                device.getMacAddress(),
                device.getCurrentIp(),
                userResponseDTO
        );
    }
}