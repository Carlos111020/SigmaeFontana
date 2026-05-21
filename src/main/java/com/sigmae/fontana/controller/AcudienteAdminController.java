package com.sigmae.fontana.controller;

import com.sigmae.fontana.dto.acudiente.AcudienteRequest;
import com.sigmae.fontana.dto.acudiente.AcudienteResponse;
import com.sigmae.fontana.service.AcudienteService;
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
@RequestMapping("/api/v1/acudientes")
@RequiredArgsConstructor
public class AcudienteAdminController {

    private final AcudienteService acudienteService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AcudienteResponse> crear(@Valid @RequestBody AcudienteRequest request) {
        var response = acudienteService.crear(request);
        return ResponseEntity.created(location(response.id())).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
    public ResponseEntity<List<AcudienteResponse>> listar(@RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(acudienteService.listar(activo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
    public ResponseEntity<AcudienteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(acudienteService.obtener(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AcudienteResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AcudienteRequest request
    ) {
        return ResponseEntity.ok(acudienteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        acudienteService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    private URI location(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
