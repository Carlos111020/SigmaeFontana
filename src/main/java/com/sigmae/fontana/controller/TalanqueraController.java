package com.sigmae.fontana.controller;

import com.sigmae.fontana.dto.talanquera.TalanqueraIngresoRequest;
import com.sigmae.fontana.dto.talanquera.TalanqueraIngresoResponse;
import com.sigmae.fontana.service.TalanqueraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/talanquera")
@RequiredArgsConstructor
public class TalanqueraController {

    private final TalanqueraService talanqueraService;

    @PostMapping("/ingresos")
    @PreAuthorize("hasAnyRole('PORTERIA','COORDINADOR')")
    public ResponseEntity<TalanqueraIngresoResponse> registrarIngreso(
            @Valid @RequestBody TalanqueraIngresoRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(talanqueraService.registrarIngreso(request, authentication.getName()));
    }
}
