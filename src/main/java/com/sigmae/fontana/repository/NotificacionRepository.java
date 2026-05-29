package com.sigmae.fontana.repository;

import com.sigmae.fontana.entity.Notificacion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByAcudienteUsuarioCorreoOrderByFechaHoraDesc(String correo);

    Optional<Notificacion> findByIdAndAcudienteUsuarioCorreo(Long id, String correo);

    @Query("""
            select notificacion
            from Notificacion notificacion
            left join notificacion.acudiente.usuario usuario
            where usuario.correo = :correo
               or notificacion.acudiente.correo = :correo
            order by notificacion.fechaHora desc
            """)
    List<Notificacion> findMisNotificacionesDeAcudiente(@Param("correo") String correo);

    @Query("""
            select notificacion
            from Notificacion notificacion
            left join notificacion.acudiente.usuario usuario
            where notificacion.id = :id
              and (
                    usuario.correo = :correo
                    or notificacion.acudiente.correo = :correo
              )
            """)
    Optional<Notificacion> findMiNotificacionDeAcudiente(
            @Param("id") Long id,
            @Param("correo") String correo
    );
}
