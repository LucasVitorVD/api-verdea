package com.verdea.api_verdea.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "Detalhes do erro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {

    @Schema(description = "Momento em que o erro ocorreu", example = "2025-07-04T13:45:00")
    public LocalDateTime timestamp;

    @Schema(description = "Código de status HTTP", example = "404")
    public int status;

    @Schema(description = "Nome do erro", example = "User not found")
    public String error;

    @Schema(description = "Mensagem explicando o erro", example = "No user found with email x@x.com")
    public String message;
}