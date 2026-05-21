package com.sigmae.fontana.repository;

import com.sigmae.fontana.entity.Novedad;
import com.sigmae.fontana.entity.enums.EstadoNovedad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NovedadRepository extends JpaRepository<Novedad, Long> {

    long countByEstado(EstadoNovedad estado);
}
