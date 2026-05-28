package com.sigmae.fontana.controller;

import com.sigmae.fontana.dto.novedad.EstadoNovedadRequest;
import com.sigmae.fontana.dto.novedad.NovedadRequest;
import com.sigmae.fontana.dto.novedad.NovedadResponse;
import com.sigmae.fontana.entity.enums.EstadoNovedad;
import com.sigmae.fontana.entity.enums.TipoNovedad;
import com.sigmae.fontana.service.NovedadService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/novedades")
@RequiredArgsConstructor
public class NovedadController {

    private final NovedadService novedadService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
    public ResponseEntity<List<NovedadResponse>> listar(
            @RequestParam(required = false) EstadoNovedad estado,
            @RequestParam(required = false) Long estudianteId,
            @RequestParam(required = false) TipoNovedad tipoNovedad
    ) {
        return ResponseEntity.ok(novedadService.listar(estado, estudianteId, tipoNovedad));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR','PORTERIA')")
    public ResponseEntity<NovedadResponse> crear(
            @Valid @RequestBody NovedadRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(novedadService.crear(request, authentication.getName()));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
    public ResponseEntity<NovedadResponse> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoNovedadRequest request
    ) {
        return ResponseEntity.ok(novedadService.actualizarEstado(id, request));
    }
}
