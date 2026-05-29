package com.sigmae.fontana.repository;

import com.sigmae.fontana.entity.EstudianteAcudiente;
import com.sigmae.fontana.entity.EstudianteAcudienteId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EstudianteAcudienteRepository extends JpaRepository<EstudianteAcudiente, EstudianteAcudienteId> {

    List<EstudianteAcudiente> findByEstudianteId(Long estudianteId);

    List<EstudianteAcudiente> findByAcudienteUsuarioCorreo(String correo);

    @Query("""
            select relacion
            from EstudianteAcudiente relacion
            left join relacion.acudiente.usuario usuario
            where usuario.correo = :correo
               or relacion.acudiente.correo = :correo
            """)
    List<EstudianteAcudiente> findMisRelacionesDeAcudiente(@Param("correo") String correo);
}
