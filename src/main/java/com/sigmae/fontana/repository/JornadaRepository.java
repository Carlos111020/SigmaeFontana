package com.sigmae.fontana.repository;

import com.sigmae.fontana.entity.Jornada;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JornadaRepository extends JpaRepository<Jornada, Long> {

    Optional<Jornada> findByFecha(LocalDate fecha);
}
