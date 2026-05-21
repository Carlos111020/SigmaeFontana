package com.sigmae.fontana.repository;

import com.sigmae.fontana.entity.Notificacion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByAcudienteUsuarioCorreoOrderByFechaHoraDesc(String correo);

    Optional<Notificacion> findByIdAndAcudienteUsuarioCorreo(Long id, String correo);
}
