package com.verdea.api_verdea.dtos.userDto;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String email,
        LocalDateTime createdAt
) {}
