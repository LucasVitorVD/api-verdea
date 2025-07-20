package com.verdea.api_verdea.dtos.deviceDto;

import java.time.LocalDateTime;

public record DeviceAssignmentResponseDTO(
        String name,
        String macAddress,
        String assignedToEmail,
        LocalDateTime assignedAt
) {}