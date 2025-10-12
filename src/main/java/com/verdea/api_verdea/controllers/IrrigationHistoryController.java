package com.verdea.api_verdea.controllers;

import com.verdea.api_verdea.config.SecurityConfig;
import com.verdea.api_verdea.dtos.irrigationHistoryDto.IrrigationHistoryRequestDTO;
import com.verdea.api_verdea.dtos.irrigationHistoryDto.IrrigationHistoryResponseDTO;
import com.verdea.api_verdea.services.irrigationHistory.IrrigationHistoryService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/irrigation-history")
@RequiredArgsConstructor
@Tag(name = "Irrigation History", description = "Endpoints for managing irrigation history data")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class IrrigationHistoryController {
    private final IrrigationHistoryService irrigationHistoryService;

    @PostMapping("/add")
    public ResponseEntity<Void> saveNewIrrigationHistory(@RequestBody IrrigationHistoryRequestDTO data) {
        irrigationHistoryService.saveIrrigation(data);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<Page<IrrigationHistoryResponseDTO>> getAllUserIrrigationHistory(Pageable pageable, @Parameter(hidden = true) Authentication auth) {
        String userEmail = auth.getName();

        Page<IrrigationHistoryResponseDTO> irrigationHistoryPage = irrigationHistoryService.getHistoryByUserId(pageable, userEmail);

        return ResponseEntity.ok(irrigationHistoryPage);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteIrrigationHistory(@PathVariable Long id) {
        irrigationHistoryService.deleteHistory(id);

        return ResponseEntity.ok().build();
    }
}
