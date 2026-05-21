package com.sigmae.fontana.repository;

import com.sigmae.fontana.entity.PuntoAcceso;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PuntoAccesoRepository extends JpaRepository<PuntoAcceso, Long> {

    Optional<PuntoAcceso> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
