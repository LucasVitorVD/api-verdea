package com.verdea.api_verdea.dtos.userDto;

import com.verdea.api_verdea.enums.Role;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String email,
        Role role,
        LocalDateTime createdAt
) {}
