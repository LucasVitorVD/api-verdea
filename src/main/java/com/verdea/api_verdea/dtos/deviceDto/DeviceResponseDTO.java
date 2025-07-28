package com.verdea.api_verdea.dtos.deviceDto;

import java.time.LocalDateTime;

public record DeviceResponseDTO(Long id, String name, String macAddress, LocalDateTime createdAt) {}
