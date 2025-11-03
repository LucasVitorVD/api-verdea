package com.verdea.api_verdea.dtos.deviceDto;

import java.time.LocalDateTime;

public record DeviceResponseAdminDTO (
        Long id,
        String name,
        String macAddress,
        String currentIp,
        LocalDateTime createdAt,
        boolean isOnline,
        String userEmail,
        String plantName
) {}
