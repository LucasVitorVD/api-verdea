package com.verdea.api_verdea.controllers;

import com.verdea.api_verdea.config.SecurityConfig;
import com.verdea.api_verdea.dtos.plantDto.PlantRequestDTO;
import com.verdea.api_verdea.dtos.plantDto.PlantResponseDTO;
import com.verdea.api_verdea.services.plant.PlantService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/all")
    public ResponseEntity<List<PlantResponseDTO>> getPlants(@Parameter(hidden = true) Authentication auth) {
        List<PlantResponseDTO> plants = plantService.getPlantsByUserEmail(auth.getName());

        return ResponseEntity.ok(plants);
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<PlantResponseDTO> updatePlant(@PathVariable Long id, @RequestBody @Valid PlantRequestDTO plantRequestDTO) {
        PlantResponseDTO response = plantService.updatePlantData(id, plantRequestDTO);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePlant(@PathVariable Long id) {
        plantService.deletePlant(id);

        return ResponseEntity.ok().build();
    }
}
