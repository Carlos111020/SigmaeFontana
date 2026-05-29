package com.sigmae.fontana.controller;

import com.sigmae.fontana.dto.estudiante.EstudianteAcudienteRequest;
import com.sigmae.fontana.dto.estudiante.EstudianteAcudienteResponse;
import com.sigmae.fontana.dto.estudiante.EstudianteRequest;
import com.sigmae.fontana.dto.estudiante.EstudianteResponse;
import com.sigmae.fontana.service.EstudianteService;
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
@RequestMapping("/api/v1/estudiantes")
@RequiredArgsConstructor
public class EstudianteController {

    private final EstudianteService estudianteService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<EstudianteResponse> crear(@Valid @RequestBody EstudianteRequest request) {
        var response = estudianteService.crear(request);
        return ResponseEntity.created(location(response.id())).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
    public ResponseEntity<List<EstudianteResponse>> listar(@RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(estudianteService.listar(activo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
    public ResponseEntity<EstudianteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(estudianteService.obtener(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<EstudianteResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EstudianteRequest request
    ) {
        return ResponseEntity.ok(estudianteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        estudianteService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/presentes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
    public ResponseEntity<List<EstudianteResponse>> presentes(
            @RequestParam(required = false) Long gradoId,
            @RequestParam(required = false) String texto
    ) {
        return ResponseEntity.ok(estudianteService.presentes(gradoId, texto));
    }

    @PostMapping("/{id}/acudientes")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> asociarAcudiente(
            @PathVariable Long id,
            @Valid @RequestBody EstudianteAcudienteRequest request
    ) {
        estudianteService.asociarAcudiente(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/acudientes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
    public ResponseEntity<List<EstudianteAcudienteResponse>> listarAcudientes(@PathVariable Long id) {
        return ResponseEntity.ok(estudianteService.listarAcudientes(id));
    }

    @PutMapping("/{id}/acudientes/{acudienteId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<EstudianteAcudienteResponse> actualizarAcudiente(
            @PathVariable Long id,
            @PathVariable Long acudienteId,
            @Valid @RequestBody EstudianteAcudienteRequest request
    ) {
        return ResponseEntity.ok(estudianteService.actualizarAcudiente(id, acudienteId, request));
    }

    @DeleteMapping("/{id}/acudientes/{acudienteId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarAcudiente(
            @PathVariable Long id,
            @PathVariable Long acudienteId
    ) {
        estudianteService.eliminarAcudiente(id, acudienteId);
        return ResponseEntity.noContent().build();
    }

    private URI location(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
