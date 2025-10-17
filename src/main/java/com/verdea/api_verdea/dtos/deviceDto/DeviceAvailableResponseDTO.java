package com.verdea.api_verdea.dtos.deviceDto;

import com.verdea.api_verdea.dtos.userDto.UserResponseDTO;

public record DeviceAvailableResponseDTO(
        Long id,
        String name,
        String macAddress,
        String currentIp,
        UserResponseDTO user
) {
}
