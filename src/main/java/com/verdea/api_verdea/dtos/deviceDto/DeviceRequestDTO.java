package com.verdea.api_verdea.dtos.deviceDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DeviceRequestDTO(

        @NotBlank(message = "Nome do dispositivo é obrigatório")
        String name,

        @NotBlank(message = "Endereço MAC é obrigatório")
        @Pattern(
                regexp = "^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$",
                message = "Endereço MAC inválido. Formato esperado: XX:XX:XX:XX:XX:XX"
        )
        String macAddress,

        @NotBlank(message = "Endereço IP é obrigatório")
        String currentIp
) {}