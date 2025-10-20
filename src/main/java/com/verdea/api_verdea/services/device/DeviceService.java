package com.verdea.api_verdea.services.device;

import com.verdea.api_verdea.dtos.deviceDto.DeviceAssignmentResponseDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceRequestDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceResponseDTO;
import com.verdea.api_verdea.dtos.deviceDto.SendMacRequest;
import com.verdea.api_verdea.dtos.plantDto.PlantSummary;
import com.verdea.api_verdea.entities.Device;
import com.verdea.api_verdea.entities.User;
import com.verdea.api_verdea.enums.DeviceStatus;
import com.verdea.api_verdea.exceptions.*;
import com.verdea.api_verdea.repositories.DeviceRepository;
import com.verdea.api_verdea.repositories.UserRepository;
import com.verdea.api_verdea.services.email.EmailService;
import com.verdea.api_verdea.services.mqtt.MqttService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final MqttService mqttService;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, UserRepository userRepository, EmailService emailService, @Lazy MqttService mqttService) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.mqttService = mqttService;
    }

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
                .currentIp(dto.currentIp())
                .status(DeviceStatus.ONLINE)
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
    public void updateDevice(Long deviceId, DeviceRequestDTO dto) {
        Device device = deviceRepository.findById(deviceId)
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

        mapToDeviceResponseDTO(updatedDevice);
    }

    public void updateDeviceStatus(String macAddress, DeviceStatus newStatus) {
        Device device = deviceRepository.findByMacAddress(macAddress)
                .orElseThrow(() -> new DeviceNotFoundException("Dispositivo não encontrado."));

        device.setStatus(newStatus);
        deviceRepository.save(device);
    }

    @Transactional
    public void deleteDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Dispositivo não encontrado."));

        device.setUser(null);
        device.setPlant(null);

        deviceRepository.save(device);
    }

    public void resetWifiMqtt(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Dispositivo não encontrado."));

        String topic = "verdea/commands/" + device.getMacAddress().replace(":", "");
        String payload = "RESET_WIFI";

        mqttService.publish(topic, payload);
    }

    public void sendEmailWithMacAddress(SendMacRequest macRequest) {
        User user = userRepository.findByEmail(macRequest.email())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        String subject = "Dados do dispositivo";
        String body = "Nome do dispositivo: \"" + macRequest.deviceName() + "\nMAC: " + macRequest.macAddress();

        emailService.sendEmail(user.getEmail(), subject, body);
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

        boolean isOnline = device.getStatus() == DeviceStatus.ONLINE;

        return new DeviceResponseDTO(
                device.getId(),
                device.getName(),
                device.getMacAddress(),
                device.getCurrentIp(),
                device.getCreatedAt(),
                isOnline,
                plantSummary
        );
    }
}
