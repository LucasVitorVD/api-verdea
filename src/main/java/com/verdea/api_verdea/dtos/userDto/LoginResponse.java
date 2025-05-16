package com.verdea.api_verdea.dtos.userDto;

import java.util.UUID;

public record LoginResponse(String accessToken, UUID refreshToken) {
}
