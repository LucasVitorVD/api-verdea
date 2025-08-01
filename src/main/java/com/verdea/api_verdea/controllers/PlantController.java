package com.verdea.api_verdea.controllers;

import com.verdea.api_verdea.config.SecurityConfig;
import com.verdea.api_verdea.dtos.plantDto.PlantRequestDTO;
import com.verdea.api_verdea.services.plant.PlantService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plant")
@RequiredArgsConstructor
@Tag(name = "Plant", description = "Endpoints for managing plant data")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class PlantController {
    private final PlantService plantService;

    @PostMapping("/add")
    public ResponseEntity<Void> addNewPlant(@RequestBody @Valid PlantRequestDTO dto, @Parameter(hidden = true) Authentication auth) {
        String userEmail = auth.getName();

        plantService.addPlant(dto, userEmail);

        return ResponseEntity.ok().build();
    }
}
