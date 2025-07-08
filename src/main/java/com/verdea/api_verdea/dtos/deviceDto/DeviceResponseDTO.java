package com.verdea.api_verdea.dtos.deviceDto;

import com.verdea.api_verdea.enums.DeviceStatus;

public record DeviceResponseDTO(String name, String macAddress, DeviceStatus status) {}
