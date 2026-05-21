package com.sigmae.fontana.controller;

import com.sigmae.fontana.dto.dashboard.DashboardResponse;
import com.sigmae.fontana.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/metricas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
    public ResponseEntity<DashboardResponse> metricas() {
        return ResponseEntity.ok(dashboardService.metricas());
    }
}
