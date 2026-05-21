package com.sigmae.fontana.repository;

import com.sigmae.fontana.entity.Estudiante;
import com.sigmae.fontana.entity.enums.EstadoPermanencia;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    Optional<Estudiante> findByCodigoEstudiantil(String codigoEstudiantil);

    Optional<Estudiante> findByDocumento(String documento);

    List<Estudiante> findByEstadoPermanencia(EstadoPermanencia estadoPermanencia);

    boolean existsByCodigoEstudiantil(String codigoEstudiantil);

    boolean existsByDocumento(String documento);

    boolean existsByGradoIdAndActivoTrue(Long gradoId);
}
