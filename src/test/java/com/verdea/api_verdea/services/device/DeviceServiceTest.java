package com.verdea.api_verdea.services.device;

import com.verdea.api_verdea.dtos.deviceDto.DeviceRequestDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceResponseDTO;
import com.verdea.api_verdea.entities.Device;
import com.verdea.api_verdea.enums.DeviceStatus;
import com.verdea.api_verdea.repositories.DeviceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
class DeviceServiceTest {
    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceService deviceService;

    @Test
    @DisplayName("Should add new device")
    void registerDeviceCase1() {
        Device device = new Device();
        device.setId(1L);
        device.setName("Dispositivo-verdea");
        device.setStatus(DeviceStatus.valueOf("ONLINE"));
        device.setMacAddress("B4:E8:F5:2W:05:F8");

        DeviceRequestDTO deviceRequestDTO = new DeviceRequestDTO(device.getName(), device.getMacAddress(), device.getStatus());

        when(deviceRepository.existsByMacAddress(device.getMacAddress())).thenReturn(false);
        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        DeviceResponseDTO response = deviceService.registerDevice(deviceRequestDTO);

        assertEquals(deviceRequestDTO.name(), response.name());
        assertEquals(deviceRequestDTO.macAddress(), response.macAddress());
        assertEquals("ONLINE", response.status().toString());
    }
}