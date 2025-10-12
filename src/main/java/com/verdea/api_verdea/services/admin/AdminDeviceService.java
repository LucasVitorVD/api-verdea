package com.verdea.api_verdea.services.admin;

import com.verdea.api_verdea.dtos.deviceDto.DeviceRequestDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceResponseDTO;
import com.verdea.api_verdea.entities.Device;
import com.verdea.api_verdea.enums.DeviceStatus;
import com.verdea.api_verdea.exceptions.DeviceAlreadyExistsException;
import com.verdea.api_verdea.exceptions.DeviceNotFoundException;
import com.verdea.api_verdea.repositories.DeviceRepository;
import com.verdea.api_verdea.services.mqtt.MqttService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDeviceService {
    private final DeviceRepository deviceRepository;
    private final MqttService mqttService;

    // Listar todos dispositivos
    public List<DeviceResponseDTO> getAllDevices() {
        return deviceRepository.findAll().stream()
                .map(this::mapToDeviceResponseDTO)
                .toList();
    }

    public DeviceResponseDTO getDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Dispositivo não encontrado."));
        return mapToDeviceResponseDTO(device);
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
}