package com.sigmae.fontana.repository;

import com.sigmae.fontana.entity.Grado;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradoRepository extends JpaRepository<Grado, Long> {

    Optional<Grado> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
