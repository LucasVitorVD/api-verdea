package com.verdea.api_verdea.controllers;

import com.verdea.api_verdea.dtos.dashboardDto.DashboardResponseDTO;
import com.verdea.api_verdea.services.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> getDashboard(Authentication authentication) {
        String userEmail = authentication.getName();
        DashboardResponseDTO response = dashboardService.getDashboardData(userEmail);

        return ResponseEntity.ok(response);
    }
}
