package com.sigmae.fontana;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sigmae.fontana.entity.Acudiente;
import com.sigmae.fontana.entity.Estudiante;
import com.sigmae.fontana.entity.EstudianteAcudiente;
import com.sigmae.fontana.entity.EstudianteAcudienteId;
import com.sigmae.fontana.entity.enums.EstadoPermanencia;
import com.sigmae.fontana.repository.AcudienteRepository;
import com.sigmae.fontana.repository.EstudianteAcudienteRepository;
import com.sigmae.fontana.repository.EstudianteRepository;
import com.sigmae.fontana.repository.GradoRepository;
import com.sigmae.fontana.repository.PuntoAccesoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.seed.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PuntoAccesoRepository puntoAccesoRepository;

    @Autowired
    private GradoRepository gradoRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private AcudienteRepository acudienteRepository;

    @Autowired
    private EstudianteAcudienteRepository estudianteAcudienteRepository;

    @Test
    void adminCreaCatalogosYEstudiante() throws Exception {
        var token = login("admin@sigmae.edu.co", "Admin123*");

        var gradoBody = """
                {
                  "nombre": "6A",
                  "nivel": "Basica secundaria"
                }
                """;
        var gradoResponse = mockMvc.perform(post("/api/v1/grados")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gradoBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.nombre").value("6A"))
                .andReturn();
        var gradoId = objectMapper.readTree(gradoResponse.getResponse().getContentAsString()).get("id").asLong();

        var puntoBody = """
                {
                  "nombre": "Porteria secundaria",
                  "ubicacion": "Entrada lateral"
                }
                """;
        mockMvc.perform(post("/api/v1/puntos-acceso")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(puntoBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.activo").value(true));

        var estudianteBody = """
                {
                  "codigoEstudiantil": "EST-900",
                  "documento": "1099000900",
                  "nombres": "Camila",
                  "apellidos": "Rojas",
                  "gradoId": %d
                }
                """.formatted(gradoId);
        var estudianteResponse = mockMvc.perform(post("/api/v1/estudiantes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estudianteBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.codigoEstudiantil").value("EST-900"))
                .andReturn();
        var estudianteId = objectMapper.readTree(estudianteResponse.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/v1/estudiantes/{id}", estudianteId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documento").value("1099000900"));
    }

    @Test
    void coordinadorNoPuedeCrearUsuario() throws Exception {
        var token = login("coordinador@sigmae.edu.co", "Coord123*");
        var body = """
                {
                  "nombres": "Auxiliar",
                  "apellidos": "Prueba",
                  "correo": "auxiliar.prueba@sigmae.edu.co",
                  "password": "Auxiliar123*",
                  "rol": "PORTERIA"
                }
                """;

        mockMvc.perform(post("/api/v1/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void noPermiteConsultarDashboardSinToken() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/metricas"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rechazaLoginConCredencialesInvalidas() throws Exception {
        var body = """
                {
                  "correo": "admin@sigmae.edu.co",
                  "password": "PasswordIncorrecto123*"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void porteriaRegistraIngresoYBloqueaDobleIngreso() throws Exception {
        var token = login("porteria@sigmae.edu.co", "Porteria123*");
        var puntoAccesoId = puntoAccesoRepository.findByNombre("Porteria principal")
                .orElseThrow()
                .getId();

        var body = """
                {
                  "identificadorEstudiante": "EST-001",
                  "puntoAccesoId": %d,
                  "observacion": "Ingreso de prueba",
                  "crearNovedadSalidaAnticipada": false
                }
                """.formatted(puntoAccesoId);

        mockMvc.perform(post("/api/v1/registros-acceso/ingresos")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoRegistro").value("INGRESO"));

        mockMvc.perform(post("/api/v1/registros-acceso/ingresos")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void talanqueraRegistraIngresoYDevuelveAcudienteNotificado() throws Exception {
        crearTarjetaDePruebaEnTest();
        var token = login("porteria@sigmae.edu.co", "Porteria123*");
        var body = """
                {
                  "codigoTarjeta": "EST-002",
                  "observacion": "Ingreso desde simulador web"
                }
                """;

        mockMvc.perform(post("/api/v1/talanquera/ingresos")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoTarjeta").value("EST-002"))
                .andExpect(jsonPath("$.estudiante").value("Mateo Rojas"))
                .andExpect(jsonPath("$.acudientesNotificados[0].correo").value("andres.rojas@example.com"))
                .andExpect(jsonPath("$.acudientesNotificados[0].correoSimuladoEnviado").value(true));
    }

    private void crearTarjetaDePruebaEnTest() {
        if (estudianteRepository.existsByCodigoEstudiantil("EST-002")) {
            return;
        }

        var grado = gradoRepository.findByNombre("5A").orElseThrow();
        var estudiante = new Estudiante();
        estudiante.setCodigoEstudiantil("EST-002");
        estudiante.setDocumento("100000002");
        estudiante.setNombres("Mateo");
        estudiante.setApellidos("Rojas");
        estudiante.setEstadoPermanencia(EstadoPermanencia.FUERA_DEL_PLANTEL);
        estudiante.setGrado(grado);
        estudianteRepository.save(estudiante);

        var acudiente = new Acudiente();
        acudiente.setDocumento("52000002");
        acudiente.setNombres("Andres");
        acudiente.setApellidos("Rojas");
        acudiente.setTelefono("3002223344");
        acudiente.setCorreo("andres.rojas@example.com");
        acudienteRepository.save(acudiente);

        var id = new EstudianteAcudienteId();
        id.setEstudianteId(estudiante.getId());
        id.setAcudienteId(acudiente.getId());
        var relacion = new EstudianteAcudiente();
        relacion.setId(id);
        relacion.setEstudiante(estudiante);
        relacion.setAcudiente(acudiente);
        relacion.setParentesco("Padre");
        relacion.setResponsablePrincipal(true);
        estudianteAcudienteRepository.save(relacion);
    }

    private String login(String correo, String password) throws Exception {
        var body = """
                {
                  "correo": "%s",
                  "password": "%s"
                }
                """.formatted(correo, password);
        var response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        return objectMapper.readTree(response.getResponse().getContentAsString()).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
