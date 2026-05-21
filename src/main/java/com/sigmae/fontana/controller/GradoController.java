package com.sigmae.fontana.controller;

import com.sigmae.fontana.dto.grado.GradoRequest;
import com.sigmae.fontana.dto.grado.GradoResponse;
import com.sigmae.fontana.service.GradoService;
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
@RequestMapping("/api/v1/grados")
@RequiredArgsConstructor
public class GradoController {

    private final GradoService gradoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<GradoResponse> crear(@Valid @RequestBody GradoRequest request) {
        var response = gradoService.crear(request);
        return ResponseEntity.created(location(response.id())).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
    public ResponseEntity<List<GradoResponse>> listar(@RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(gradoService.listar(activo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
    public ResponseEntity<GradoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(gradoService.obtener(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<GradoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody GradoRequest request
    ) {
        return ResponseEntity.ok(gradoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        gradoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    private URI location(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
