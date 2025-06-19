package com.verdea.api_verdea.dtos.userDto;

public record ResetPasswordRequest(String token, String newPassword) {
}
