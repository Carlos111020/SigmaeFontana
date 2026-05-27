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
        if (!seedEnabled) {
            return;
        }

        var admin = usuario("Admin", "SIGMAE", "admin@sigmae.edu.co", "Admin123*", RolUsuario.ADMINISTRADOR);
        usuario("Coordinacion", "Fontana", "coordinador@sigmae.edu.co", "Coord123*", RolUsuario.COORDINADOR);
        usuario("Porteria", "Principal", "porteria@sigmae.edu.co", "Porteria123*", RolUsuario.PORTERIA);
        var usuarioAcudiente = usuario("Laura", "Gomez", "acudiente@sigmae.edu.co", "Acudiente123*", RolUsuario.ACUDIENTE);

        var grado = gradoRepository.findByNombre("5A").orElseGet(() -> {
            var nuevoGrado = new Grado();
            nuevoGrado.setNombre("5A");
            nuevoGrado.setNivel("Primaria");
            return gradoRepository.save(nuevoGrado);
        });

        puntoAccesoRepository.findByNombre("Porteria principal").orElseGet(() -> {
            var puntoAcceso = new PuntoAcceso();
            puntoAcceso.setNombre("Porteria principal");
            puntoAcceso.setUbicacion("Entrada principal del plantel");
            return puntoAccesoRepository.save(puntoAcceso);
        });

        jornadaRepository.findByFecha(LocalDate.now()).orElseGet(() -> {
            var jornada = new Jornada();
            jornada.setFecha(LocalDate.now());
            jornada.setHoraInicio(LocalTime.of(6, 0));
            jornada.setHoraFin(LocalTime.of(15, 0));
            jornada.setEstado(EstadoJornada.ABIERTA);
            return jornadaRepository.save(jornada);
        });

        crearEstudianteConAcudiente(
                "EST-001", "100000001", "Sofia", "Gomez",
                "52000001", "Laura", "Gomez", "3001234567", "laura.gomez@example.com",
                usuarioAcudiente, grado, "Madre"
        );
        crearEstudianteConAcudiente(
                "EST-002", "100000002", "Mateo", "Rojas",
                "52000002", "Andres", "Rojas", "3002223344", "andres.rojas@example.com",
                null, grado, "Padre"
        );
        crearEstudianteConAcudiente(
                "EST-003", "100000003", "Valentina", "Perez",
                "52000003", "Claudia", "Perez", "3003334455", "claudia.perez@example.com",
                null, grado, "Madre"
        );
        crearEstudianteConAcudiente(
                "EST-004", "100000004", "Juan", "Martinez",
                "52000004", "Ricardo", "Martinez", "3004445566", "ricardo.martinez@example.com",
                null, grado, "Padre"
        );
        crearEstudianteConAcudiente(
                "EST-005", "100000005", "Isabella", "Torres",
                "52000005", "Patricia", "Torres", "3005556677", "patricia.torres@example.com",
                null, grado, "Madre"
        );

        admin.setActivo(true);
    }

    private Usuario usuario(String nombres, String apellidos, String correo, String password, RolUsuario rol) {
        var existente = usuarioRepository.findByCorreo(correo);
        if (existente.isPresent()) {
            return existente.get();
        }

        var usuario = new Usuario();
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    private void crearEstudianteConAcudiente(
            String codigoEstudiantil,
            String documentoEstudiante,
            String nombresEstudiante,
            String apellidosEstudiante,
            String documentoAcudiente,
            String nombresAcudiente,
            String apellidosAcudiente,
            String telefonoAcudiente,
            String correoAcudiente,
            Usuario usuarioAcudiente,
            Grado grado,
            String parentesco
    ) {
        if (estudianteRepository.existsByCodigoEstudiantil(codigoEstudiantil)) {
            return;
        }

        var estudiante = new Estudiante();
        estudiante.setCodigoEstudiantil(codigoEstudiantil);
        estudiante.setDocumento(documentoEstudiante);
        estudiante.setNombres(nombresEstudiante);
        estudiante.setApellidos(apellidosEstudiante);
        estudiante.setEstadoPermanencia(EstadoPermanencia.FUERA_DEL_PLANTEL);
        estudiante.setGrado(grado);
        estudianteRepository.save(estudiante);

        var acudiente = new Acudiente();
        acudiente.setDocumento(documentoAcudiente);
        acudiente.setNombres(nombresAcudiente);
        acudiente.setApellidos(apellidosAcudiente);
        acudiente.setTelefono(telefonoAcudiente);
        acudiente.setCorreo(correoAcudiente);
        acudiente.setUsuario(usuarioAcudiente);
        acudienteRepository.save(acudiente);

        var id = new EstudianteAcudienteId();
        id.setEstudianteId(estudiante.getId());
        id.setAcudienteId(acudiente.getId());
        var relacion = new EstudianteAcudiente();
        relacion.setId(id);
        relacion.setEstudiante(estudiante);
        relacion.setAcudiente(acudiente);
        relacion.setParentesco(parentesco);
        relacion.setResponsablePrincipal(true);
        estudianteAcudienteRepository.save(relacion);
    }
}
