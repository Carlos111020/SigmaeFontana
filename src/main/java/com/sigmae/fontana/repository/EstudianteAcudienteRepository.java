package com.sigmae.fontana.repository;

import com.sigmae.fontana.entity.EstudianteAcudiente;
import com.sigmae.fontana.entity.EstudianteAcudienteId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstudianteAcudienteRepository extends JpaRepository<EstudianteAcudiente, EstudianteAcudienteId> {

    List<EstudianteAcudiente> findByEstudianteId(Long estudianteId);

    List<EstudianteAcudiente> findByAcudienteUsuarioCorreo(String correo);
}
