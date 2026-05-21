package com.sigmae.fontana.controller;

import com.sigmae.fontana.dto.acudiente.AcudienteStudentResponse;
import com.sigmae.fontana.dto.notificacion.NotificacionResponse;
import com.sigmae.fontana.service.AcudienteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/acudientes/me")
@RequiredArgsConstructor
public class AcudienteController {

    private final AcudienteService acudienteService;

    @GetMapping("/estudiantes")
    @PreAuthorize("hasRole('ACUDIENTE')")
    public ResponseEntity<List<AcudienteStudentResponse>> misEstudiantes(Authentication authentication) {
        return ResponseEntity.ok(acudienteService.misEstudiantes(authentication.getName()));
    }

    @GetMapping("/notificaciones")
    @PreAuthorize("hasRole('ACUDIENTE')")
    public ResponseEntity<List<NotificacionResponse>> misNotificaciones(Authentication authentication) {
        return ResponseEntity.ok(acudienteService.misNotificaciones(authentication.getName()));
    }

    @PatchMapping("/notificaciones/{id}/leida")
    @PreAuthorize("hasRole('ACUDIENTE')")
    public ResponseEntity<NotificacionResponse> marcarNotificacionLeida(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(acudienteService.marcarNotificacionLeida(id, authentication.getName()));
    }
}
