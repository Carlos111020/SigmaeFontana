package com.sigmae.fontana.repository;

import com.sigmae.fontana.entity.RegistroAcceso;
import com.sigmae.fontana.entity.enums.TipoRegistro;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RegistroAccesoRepository extends JpaRepository<RegistroAcceso, Long>, JpaSpecificationExecutor<RegistroAcceso> {

    long countByTipoRegistroAndFechaHoraBetween(TipoRegistro tipoRegistro, LocalDateTime desde, LocalDateTime hasta);

    Optional<RegistroAcceso> findFirstByEstudianteIdOrderByFechaHoraDesc(Long estudianteId);

    Page<RegistroAcceso> findByEstudianteIdOrderByFechaHoraDesc(Long estudianteId, Pageable pageable);
}
