package com.sigmae.fontana.controller;

import com.sigmae.fontana.dto.puntoacceso.PuntoAccesoRequest;
import com.sigmae.fontana.dto.puntoacceso.PuntoAccesoResponse;
import com.sigmae.fontana.service.PuntoAccesoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/puntos-acceso")
@RequiredArgsConstructor
public class PuntoAccesoController {

    private final PuntoAccesoService puntoAccesoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PuntoAccesoResponse> crear(@Valid @RequestBody PuntoAccesoRequest request) {
        var response = puntoAccesoService.crear(request);
        return ResponseEntity.created(location(response.id())).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR','PORTERIA')")
    public ResponseEntity<List<PuntoAccesoResponse>> listar(@RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(puntoAccesoService.listar(activo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR','PORTERIA')")
    public ResponseEntity<PuntoAccesoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(puntoAccesoService.obtener(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PuntoAccesoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PuntoAccesoRequest request
    ) {
        return ResponseEntity.ok(puntoAccesoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        puntoAccesoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    private URI location(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
