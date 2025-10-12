package com.verdea.api_verdea.dtos.userDto;

import com.verdea.api_verdea.enums.Role;

public record UserRequestDTO(String email, String password, Role role) {
}
