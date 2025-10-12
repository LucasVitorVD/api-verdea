package com.verdea.api_verdea.dtos.userDto;

import com.verdea.api_verdea.enums.Role;

public record UpdateUserRequestDTO(String email, String password) {
}
