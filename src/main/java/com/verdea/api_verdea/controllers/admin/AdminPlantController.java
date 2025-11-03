package com.verdea.api_verdea.controllers.admin;

import com.verdea.api_verdea.dtos.plantDto.PlantRequestDTO;
import com.verdea.api_verdea.dtos.plantDto.PlantResponseDTO;
import com.verdea.api_verdea.services.admin.AdminPlantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/plants")
@RequiredArgsConstructor
@Tag(name = "Admin Plants", description = "Admin endpoints for managing plants")
public class AdminPlantController {
    private final AdminPlantService adminPlantService;

    @GetMapping
    public List<PlantResponseDTO> getAllPlants() {
        return adminPlantService.getAllPlants();
    }

    @GetMapping("/{id}")
    public PlantResponseDTO getPlantById(@PathVariable Long id) {
        return adminPlantService.getPlantById(id);
    }

    @PostMapping("/add")
    public PlantResponseDTO createPlant(@RequestBody PlantRequestDTO dto) {
        return adminPlantService.createPlant(dto);
    }

    @PatchMapping("/update/{id}")
    public PlantResponseDTO updatePlant(@PathVariable Long id, @RequestBody PlantRequestDTO dto) {
        return adminPlantService.updatePlant(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlant(@PathVariable Long id) {
        adminPlantService.deletePlant(id);
        return ResponseEntity.noContent().build();
    }
}
