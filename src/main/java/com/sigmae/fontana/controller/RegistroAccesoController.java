package com.sigmae.fontana.controller;

import com.sigmae.fontana.dto.registro.RegistroAccesoRequest;
import com.sigmae.fontana.dto.registro.RegistroAccesoResponse;
import com.sigmae.fontana.entity.enums.TipoRegistro;
import com.sigmae.fontana.service.RegistroAccesoService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/registros-acceso")
@RequiredArgsConstructor
public class RegistroAccesoController {

    private final RegistroAccesoService registroAccesoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
    public ResponseEntity<Page<RegistroAccesoResponse>> filtrar(
            @RequestParam(required = false) Long estudianteId,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(required = false) TipoRegistro tipoRegistro,
            @RequestParam(required = false) Long puntoAccesoId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(registroAccesoService.filtrar(
                estudianteId,
                fechaDesde,
                fechaHasta,
                tipoRegistro,
                puntoAccesoId,
                pageable
        ));
    }

    @PostMapping("/ingresos")
    @PreAuthorize("hasAnyRole('PORTERIA','COORDINADOR')")
    public ResponseEntity<RegistroAccesoResponse> registrarIngreso(
            @Valid @RequestBody RegistroAccesoRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registroAccesoService.registrarIngreso(request, authentication.getName()));
    }

    @PostMapping("/salidas")
    @PreAuthorize("hasAnyRole('PORTERIA','COORDINADOR')")
    public ResponseEntity<RegistroAccesoResponse> registrarSalida(
            @Valid @RequestBody RegistroAccesoRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registroAccesoService.registrarSalida(request, authentication.getName()));
    }

    @GetMapping("/estudiantes/{estudianteId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
    public ResponseEntity<Page<RegistroAccesoResponse>> historial(
            @PathVariable Long estudianteId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(registroAccesoService.historial(estudianteId, pageable));
    }
}
