package com.verdea.api_verdea.services.device;

import com.verdea.api_verdea.dtos.deviceDto.DeviceAssignmentResponseDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceRequestDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceResponseDTO;
import com.verdea.api_verdea.dtos.userDto.UserRequestDTO;
import com.verdea.api_verdea.dtos.userDto.UserResponseDTO;
import com.verdea.api_verdea.entities.Device;
import com.verdea.api_verdea.entities.User;
import com.verdea.api_verdea.enums.DeviceStatus;
import com.verdea.api_verdea.exceptions.DeviceAlreadyAssignedException;
import com.verdea.api_verdea.exceptions.DeviceAlreadyExistsException;
import com.verdea.api_verdea.exceptions.DeviceNotFoundException;
import com.verdea.api_verdea.repositories.DeviceRepository;
import com.verdea.api_verdea.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
class DeviceServiceTest {
    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeviceService deviceService;

    private final Device device = new Device();

    private final User user = new User();

    @BeforeEach
    void setup() {
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setPassword("encodedPassword");

        device.setId(1L);
        device.setName("Dispositivo-verdea");
        device.setMacAddress("B4:E8:F5:2W:05:F8");
    }

    @Test
    @DisplayName("Should add new device")
    void registerDeviceCase1() {
        DeviceRequestDTO deviceRequestDTO = new DeviceRequestDTO(device.getName(), device.getMacAddress(), device.getCurrentIp());

        when(deviceRepository.existsByMacAddress(device.getMacAddress())).thenReturn(false);
        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        DeviceResponseDTO response = deviceService.registerDevice(deviceRequestDTO);

        assertEquals(deviceRequestDTO.name(), response.name());
        assertEquals(deviceRequestDTO.macAddress(), response.macAddress());
    }

    @Test
    @DisplayName("Should throw error if device already exists")
    void registerDeviceCase2() {
        DeviceRequestDTO deviceRequestDTO = new DeviceRequestDTO(device.getName(), device.getMacAddress(), device.getCurrentIp());

        when(deviceRepository.existsByMacAddress(device.getMacAddress())).thenReturn(true);

        assertThrows(DeviceAlreadyExistsException.class, () -> deviceService.registerDevice(deviceRequestDTO));

        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    @DisplayName("Should assign device to an authenticated user")
    void assignDeviceToUserCase1() {
        when(deviceRepository.findByMacAddress(device.getMacAddress())).thenReturn(Optional.of(device));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        DeviceAssignmentResponseDTO response = deviceService.assignDeviceToUser(device.getMacAddress(), user.getEmail());

        assertEquals(user.getId(), device.getUser().getId());
        assertEquals(device.getName(), response.name());
        assertEquals(device.getMacAddress(), response.macAddress());
    }

    @Test
    @DisplayName("Should throw DeviceAlreadyAssignedException if device is already assigned")
    void assignDeviceToUserCase2() {
        device.setUser(user);

        when(deviceRepository.findByMacAddress(device.getMacAddress())).thenReturn(Optional.of(device));

        assertThrows(DeviceAlreadyAssignedException.class, () ->
                deviceService.assignDeviceToUser(device.getMacAddress(), user.getEmail())
        );

        verify(userRepository, never()).findByEmail(any());
        verify(deviceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DeviceNotFoundException if device does not exist")
    void assignDeviceToUserCase3() {
        when(deviceRepository.findByMacAddress(device.getMacAddress())).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> // substitua por DeviceNotFoundException se tiver
                deviceService.assignDeviceToUser(device.getMacAddress(), user.getEmail())
        );

        verify(userRepository, never()).findByEmail(any());
        verify(deviceRepository, never()).save(any());
    }
}