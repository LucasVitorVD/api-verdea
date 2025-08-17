package com.verdea.api_verdea.controllers;

import com.verdea.api_verdea.config.SecurityConfig;
import com.verdea.api_verdea.dtos.deviceDto.DeviceAssignmentResponseDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceRequestDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceResponseDTO;
import com.verdea.api_verdea.services.device.DeviceService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
@Tag(name = "Device", description = "Endpoints for managing device data")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class DeviceController {
    private final DeviceService deviceService;

    private static final Pattern MAC_ADDRESS_PATTERN =
            Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");


    @PostMapping("/add")
    public ResponseEntity<DeviceResponseDTO> addNewDevice(@RequestBody @Valid DeviceRequestDTO dto) {
        DeviceResponseDTO deviceResponseDTO = deviceService.registerDevice(dto);

        return ResponseEntity.ok(deviceResponseDTO);
    }

    @PatchMapping("/assign/{macAddress}")
    public ResponseEntity<DeviceAssignmentResponseDTO> assignDevice(@PathVariable String macAddress, @Parameter(hidden = true) Authentication auth) {
        if (!MAC_ADDRESS_PATTERN.matcher(macAddress).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MAC address inválido");
        }

        String email = auth.getName();

        DeviceAssignmentResponseDTO response = deviceService.assignDeviceToUser(macAddress, email);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-devices")
    public ResponseEntity<List<DeviceResponseDTO>> getMyDevices(@Parameter(hidden = true) Authentication auth) {
        String email = auth.getName();

        List<DeviceResponseDTO> response = deviceService.getDevicesByUserEmail(email);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceResponseDTO> getDeviceById(@PathVariable Long deviceId) {
        return ResponseEntity.ok(deviceService.getDeviceById(deviceId));
    }

    @PatchMapping("/update/{deviceId}")
    public ResponseEntity<Void> updateDevice(@PathVariable Long deviceId, @RequestBody DeviceRequestDTO deviceRequestDTO) {
        deviceService.updateDevice(deviceId, deviceRequestDTO);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDeviceById(id);

        return ResponseEntity.ok().build();
    }
}
