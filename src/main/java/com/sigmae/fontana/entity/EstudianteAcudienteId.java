package com.sigmae.fontana.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@EqualsAndHashCode
public class EstudianteAcudienteId implements Serializable {

    @Column(name = "estudiante_id")
    private Long estudianteId;

    @Column(name = "acudiente_id")
    private Long acudienteId;
}
