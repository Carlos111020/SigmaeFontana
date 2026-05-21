package com.sigmae.fontana.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "estudiantes_acudientes")
public class EstudianteAcudiente {

    @EmbeddedId
    private EstudianteAcudienteId id = new EstudianteAcudienteId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("estudianteId")
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("acudienteId")
    @JoinColumn(name = "acudiente_id")
    private Acudiente acudiente;

    @Column(nullable = false, length = 40)
    private String parentesco;

    @Column(nullable = false)
    private boolean responsablePrincipal;
}
