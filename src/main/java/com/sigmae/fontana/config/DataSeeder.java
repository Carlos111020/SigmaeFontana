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
import java.util.List;
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

        usuario("Admin", "SIGMAE", "admin@sigmae.edu.co", "Admin123*", RolUsuario.ADMINISTRADOR);
        usuario("Coordinacion", "Fontana", "coordinador@sigmae.edu.co", "Coord123*", RolUsuario.COORDINADOR);
        usuario("Porteria", "Principal", "porteria@sigmae.edu.co", "Porteria123*", RolUsuario.PORTERIA);
        var usuarioAcudiente = usuario("Laura", "Gomez", "acudiente@sigmae.edu.co", "Acudiente123*", RolUsuario.ACUDIENTE);

        grado("5A", "Primaria");
        var sexto = grado("6A", "Basica secundaria");
        var septimo = grado("7B", "Basica secundaria");
        var octavo = grado("8A", "Basica secundaria");
        var noveno = grado("9B", "Basica secundaria");
        var decimo = grado("10A", "Media");

        var puntoAcceso = puntoAccesoRepository.findByNombre("Porteria principal").orElseGet(() -> {
            var nuevoPuntoAcceso = new PuntoAcceso();
            nuevoPuntoAcceso.setNombre("Porteria principal");
            return nuevoPuntoAcceso;
        });
        puntoAcceso.setUbicacion("Entrada principal del plantel");
        puntoAcceso.setActivo(true);
        puntoAccesoRepository.save(puntoAcceso);

        jornadaRepository.findByFecha(LocalDate.now()).orElseGet(() -> {
            var jornada = new Jornada();
            jornada.setFecha(LocalDate.now());
            jornada.setHoraInicio(LocalTime.of(6, 0));
            jornada.setHoraFin(LocalTime.of(15, 0));
            jornada.setEstado(EstadoJornada.ABIERTA);
            return jornadaRepository.save(jornada);
        });

        List.of(
                new EstudianteSemilla(
                        "EST-001", "100000001", "Sofia", "Gomez", sexto,
                        "52000001", "Laura", "Gomez", "3001234567", "laura.gomez@example.com",
                        usuarioAcudiente, "Madre"
                ),
                new EstudianteSemilla(
                        "EST-002", "100000002", "Mateo", "Rojas", septimo,
                        "52000002", "Andres", "Rojas", "3002223344", "andres.rojas@example.com",
                        null, "Padre"
                ),
                new EstudianteSemilla(
                        "EST-003", "100000003", "Valentina", "Perez", octavo,
                        "52000003", "Claudia", "Perez", "3003334455", "claudia.perez@example.com",
                        null, "Madre"
                ),
                new EstudianteSemilla(
                        "EST-004", "100000004", "Juan", "Martinez", noveno,
                        "52000004", "Ricardo", "Martinez", "3004445566", "ricardo.martinez@example.com",
                        null, "Padre"
                ),
                new EstudianteSemilla(
                        "EST-005", "100000005", "Isabella", "Torres", decimo,
                        "52000005", "Patricia", "Torres", "3005556677", "patricia.torres@example.com",
                        null, "Madre"
                )
        ).forEach(this::crearEstudianteConAcudiente);
    }

    private Usuario usuario(String nombres, String apellidos, String correo, String password, RolUsuario rol) {
        var existente = usuarioRepository.findByCorreo(correo);
        if (existente.isPresent()) {
            var usuario = existente.get();
            usuario.setNombres(nombres);
            usuario.setApellidos(apellidos);
            usuario.setRol(rol);
            usuario.setActivo(true);
            return usuario;
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

    private Grado grado(String nombre, String nivel) {
        var existente = gradoRepository.findByNombre(nombre);
        if (existente.isPresent()) {
            var grado = existente.get();
            grado.setNivel(nivel);
            grado.setActivo(true);
            return grado;
        }
        var nuevoGrado = new Grado();
        nuevoGrado.setNombre(nombre);
        nuevoGrado.setNivel(nivel);
        nuevoGrado.setActivo(true);
        return gradoRepository.save(nuevoGrado);
    }

    private void crearEstudianteConAcudiente(EstudianteSemilla semilla) {
        var estudiante = estudianteRepository.findByCodigoEstudiantil(semilla.codigoEstudiantil())
                .or(() -> estudianteRepository.findByDocumento(semilla.documentoEstudiante()))
                .orElseGet(() -> {
                    var nuevoEstudiante = new Estudiante();
                    nuevoEstudiante.setCodigoEstudiantil(semilla.codigoEstudiantil());
                    return nuevoEstudiante;
                });
        estudiante.setCodigoEstudiantil(semilla.codigoEstudiantil());
        estudiante.setDocumento(semilla.documentoEstudiante());
        estudiante.setNombres(semilla.nombresEstudiante());
        estudiante.setApellidos(semilla.apellidosEstudiante());
        estudiante.setGrado(semilla.grado());
        estudiante.setActivo(true);
        if (estudiante.getEstadoPermanencia() == null) {
            estudiante.setEstadoPermanencia(EstadoPermanencia.FUERA_DEL_PLANTEL);
        }
        estudianteRepository.save(estudiante);

        var acudiente = acudienteRepository.findByDocumento(semilla.documentoAcudiente())
                .or(() -> acudienteRepository.findByCorreo(semilla.correoAcudiente()))
                .orElseGet(Acudiente::new);
        acudiente.setDocumento(semilla.documentoAcudiente());
        acudiente.setNombres(semilla.nombresAcudiente());
        acudiente.setApellidos(semilla.apellidosAcudiente());
        acudiente.setTelefono(semilla.telefonoAcudiente());
        acudiente.setCorreo(semilla.correoAcudiente());
        acudiente.setActivo(true);
        if (semilla.usuarioAcudiente() != null) {
            acudiente.setUsuario(semilla.usuarioAcudiente());
        }
        acudienteRepository.save(acudiente);

        var id = new EstudianteAcudienteId();
        id.setEstudianteId(estudiante.getId());
        id.setAcudienteId(acudiente.getId());
        if (estudianteAcudienteRepository.existsById(id)) {
            return;
        }

        var relacion = new EstudianteAcudiente();
        relacion.setId(id);
        relacion.setEstudiante(estudiante);
        relacion.setAcudiente(acudiente);
        relacion.setParentesco(semilla.parentesco());
        relacion.setResponsablePrincipal(true);
        estudianteAcudienteRepository.save(relacion);
    }

    private record EstudianteSemilla(
            String codigoEstudiantil,
            String documentoEstudiante,
            String nombresEstudiante,
            String apellidosEstudiante,
            Grado grado,
            String documentoAcudiente,
            String nombresAcudiente,
            String apellidosAcudiente,
            String telefonoAcudiente,
            String correoAcudiente,
            Usuario usuarioAcudiente,
            String parentesco
    ) {
    }
}
