package com.verdea.api_verdea.dtos.deviceDto;

import java.time.LocalDateTime;

public record DeviceResponseDTO(String name, String macAddress, LocalDateTime createdAt) {}
