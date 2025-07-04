package com.verdea.api_verdea.controllers;

import com.verdea.api_verdea.config.SecurityConfig;
import com.verdea.api_verdea.dtos.csrfDto.CsrfResponse;
import com.verdea.api_verdea.entities.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "CSRF", description = "Endpoints for managing csrf token")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class CsrfController {

    @GetMapping("/csrf")
    @Operation(
            summary = "get csrf token",
            description = "method to get new csrf token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Csrf token received"),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<CsrfResponse> getCsrfToken(HttpServletRequest request) {
        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        return ResponseEntity.ok(new CsrfResponse(csrf.getHeaderName(), csrf.getToken()));
    }
}