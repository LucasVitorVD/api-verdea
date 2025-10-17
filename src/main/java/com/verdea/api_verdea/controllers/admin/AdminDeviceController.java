package com.verdea.api_verdea.controllers.admin;

import com.verdea.api_verdea.dtos.deviceDto.*;
import com.verdea.api_verdea.services.admin.AdminDeviceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/admin/devices")
@RequiredArgsConstructor
@Tag(name = "Admin Devices", description = "Admin endpoints for managing devices")
public class AdminDeviceController {
    private final AdminDeviceService adminDeviceService;

    private static final Pattern MAC_ADDRESS_PATTERN =
            Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");

    @GetMapping
    public ResponseEntity<List<DeviceResponseDTO>> getAllDevices() {
        return ResponseEntity.ok(adminDeviceService.getAllDevices());
    }

    @GetMapping("/with-user")
    public ResponseEntity<List<DeviceAvailableResponseDTO>> getDevicesWithUser() {
        return ResponseEntity.ok(adminDeviceService.getDevicesWithUser());
    }

    @PatchMapping("/assign")
    public ResponseEntity<DeviceAssignmentResponseDTO> assignDevice(@RequestBody DeviceAssignmentRequestDTO requestDTO) {
        String macAddress = requestDTO.macAddress();
        String email = requestDTO.email();

        if (!MAC_ADDRESS_PATTERN.matcher(macAddress).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MAC address inválido");
        }

        DeviceAssignmentResponseDTO response = adminDeviceService.assignDeviceToUser(macAddress, email);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponseDTO> getDeviceById(@PathVariable Long id) {
        return ResponseEntity.ok(adminDeviceService.getDeviceById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponseDTO> updateDevice(@PathVariable Long id, @RequestBody DeviceRequestDTO dto) {
        return ResponseEntity.ok(adminDeviceService.updateDevice(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        adminDeviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}