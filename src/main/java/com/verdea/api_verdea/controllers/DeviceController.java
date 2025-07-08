package com.verdea.api_verdea.controllers;

import com.verdea.api_verdea.config.SecurityConfig;
import com.verdea.api_verdea.dtos.deviceDto.DeviceRequestDTO;
import com.verdea.api_verdea.dtos.deviceDto.DeviceResponseDTO;
import com.verdea.api_verdea.services.device.DeviceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
@Tag(name = "Device", description = "Endpoints for managing device data")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class DeviceController {
    private final DeviceService deviceService;

    @PostMapping("/add")
    public ResponseEntity<DeviceResponseDTO> addNewDevice(@RequestBody @Valid DeviceRequestDTO dto) {
        DeviceResponseDTO deviceResponseDTO = deviceService.registerDevice(dto);

        return ResponseEntity.ok(deviceResponseDTO);
    }
}
