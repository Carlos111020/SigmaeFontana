package com.sigmae.fontana.repository;

import com.sigmae.fontana.entity.Acudiente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcudienteRepository extends JpaRepository<Acudiente, Long> {

    Optional<Acudiente> findByUsuarioCorreo(String correo);

    Optional<Acudiente> findByUsuarioId(Long usuarioId);

    boolean existsByDocumento(String documento);

    boolean existsByCorreo(String correo);

    boolean existsByUsuarioId(Long usuarioId);
}
