package com.sigmae.fontana.config;

import com.sigmae.fontana.entity.Acudiente;
import com.sigmae.fontana.entity.Estudiante;
import com.sigmae.fontana.entity.EstudianteAcudiente;
import com.sigmae.fontana.entity.EstudianteAcudienteId;
import com.sigmae.fontana.entity.Grado;
import com.sigmae.fontana.entity.Jornada;
import com.sigmae.fontana.entity.PuntoAcceso;
import com.sigmae.fontana.entity.Usuario;
import com.sigmae.fontana.entity.enums.EstadoJornada;
import com.sigmae.fontana.entity.enums.EstadoPermanencia;
import com.sigmae.fontana.entity.enums.RolUsuario;
import com.sigmae.fontana.repository.AcudienteRepository;
import com.sigmae.fontana.repository.EstudianteAcudienteRepository;
import com.sigmae.fontana.repository.EstudianteRepository;
import com.sigmae.fontana.repository.GradoRepository;
import com.sigmae.fontana.repository.JornadaRepository;
import com.sigmae.fontana.repository.PuntoAccesoRepository;
import com.sigmae.fontana.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final GradoRepository gradoRepository;
    private final EstudianteRepository estudianteRepository;
    private final AcudienteRepository acudienteRepository;
    private final EstudianteAcudienteRepository estudianteAcudienteRepository;
    private final PuntoAccesoRepository puntoAccesoRepository;
    private final JornadaRepository jornadaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled || usuarioRepository.count() > 0) {
            return;
        }

        var admin = usuario("Admin", "SIGMAE", "admin@sigmae.edu.co", "Admin123*", RolUsuario.ADMINISTRADOR);
        usuario("Coordinacion", "Fontana", "coordinador@sigmae.edu.co", "Coord123*", RolUsuario.COORDINADOR);
        usuario("Porteria", "Principal", "porteria@sigmae.edu.co", "Porteria123*", RolUsuario.PORTERIA);
        var usuarioAcudiente = usuario("Laura", "Gomez", "acudiente@sigmae.edu.co", "Acudiente123*", RolUsuario.ACUDIENTE);

        var grado = new Grado();
        grado.setNombre("5A");
        grado.setNivel("Primaria");
        gradoRepository.save(grado);

        var puntoAcceso = new PuntoAcceso();
        puntoAcceso.setNombre("Porteria principal");
        puntoAcceso.setUbicacion("Entrada principal del plantel");
        puntoAccesoRepository.save(puntoAcceso);

        var jornada = new Jornada();
        jornada.setFecha(LocalDate.now());
        jornada.setHoraInicio(LocalTime.of(6, 0));
        jornada.setHoraFin(LocalTime.of(15, 0));
        jornada.setEstado(EstadoJornada.ABIERTA);
        jornadaRepository.save(jornada);

        var estudiante = new Estudiante();
        estudiante.setCodigoEstudiantil("EST-001");
        estudiante.setDocumento("100000001");
        estudiante.setNombres("Sofia");
        estudiante.setApellidos("Gomez");
        estudiante.setEstadoPermanencia(EstadoPermanencia.FUERA_DEL_PLANTEL);
        estudiante.setGrado(grado);
        estudianteRepository.save(estudiante);

        var acudiente = new Acudiente();
        acudiente.setDocumento("52000001");
        acudiente.setNombres("Laura");
        acudiente.setApellidos("Gomez");
        acudiente.setTelefono("3001234567");
        acudiente.setCorreo("laura.gomez@example.com");
        acudiente.setUsuario(usuarioAcudiente);
        acudienteRepository.save(acudiente);

        var id = new EstudianteAcudienteId();
        id.setEstudianteId(estudiante.getId());
        id.setAcudienteId(acudiente.getId());
        var relacion = new EstudianteAcudiente();
        relacion.setId(id);
        relacion.setEstudiante(estudiante);
        relacion.setAcudiente(acudiente);
        relacion.setParentesco("Madre");
        relacion.setResponsablePrincipal(true);
        estudianteAcudienteRepository.save(relacion);

        admin.setActivo(true);
    }

    private Usuario usuario(String nombres, String apellidos, String correo, String password, RolUsuario rol) {
        var usuario = new Usuario();
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }
}
