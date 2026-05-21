# SIGMAE Fontana

Backend Spring Boot para el control de ingreso, permanencia y salida de estudiantes del Gimnasio Campestre La Fontana.

## Integrantes

| Integrante | Rol |
| --- | --- |
| David Alejandro Avila | Lider tecnico y coordinacion general |
| Carlos Mario Pena | Requerimientos y modelo de datos |
| Danna Giselle Aguilar | Documentacion, presentacion y soporte visual |
| Daniel Ronaldo Mosquera | Seguridad, backend y pruebas de API |

## Stack

- Java 21 + Spring Boot 3.x.
- Spring Data JPA + Hibernate.
- PostgreSQL 16.
- Spring Security 6 + JWT.
- Bean Validation.
- MapStruct.
- SpringDoc OpenAPI / Swagger UI.
- Maven Wrapper.

## Arquitectura

- `controller`: endpoints REST versionados con prefijo `/api/v1`.
- `service` y `service.impl`: reglas de negocio y transacciones.
- `repository`: acceso a datos con Spring Data JPA.
- `entity`: modelo JPA del diseno entidad-relacion.
- `dto`: entradas y salidas de la API. No se exponen entidades JPA.
- `mapper`: conversion de entidades a DTO usando MapStruct.
- `security`: autenticacion JWT, BCrypt y autorizacion por roles.
- `exception`: respuestas de error estandarizadas.

## Ejecucion local

Crear la base de datos:

```sql
CREATE DATABASE sigmae_fontana;
```

Variables opcionales:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sigmae_fontana
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
APP_JWT_SECRET=SIGMAE_FONTANA_SECRET_KEY_FOR_JWT_2026_MINIMUM_256_BITS
APP_JWT_EXPIRATION_MINUTES=480
APP_SEED_ENABLED=true
```

Ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

## Pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas usan H2 en modo PostgreSQL y cubren arranque de contexto, login JWT, creacion de catalogos/estudiantes y el flujo de ingreso con bloqueo de doble ingreso.

## Usuarios semilla

Al iniciar con una base vacia y `APP_SEED_ENABLED=true` se crean datos de prueba:

| Rol | Correo | Password |
| --- | --- | --- |
| ADMINISTRADOR | admin@sigmae.edu.co | Admin123* |
| COORDINADOR | coordinador@sigmae.edu.co | Coord123* |
| PORTERIA | porteria@sigmae.edu.co | Porteria123* |
| ACUDIENTE | acudiente@sigmae.edu.co | Acudiente123* |

## Endpoints principales

Autenticacion:

- `POST /api/v1/auth/login`

Administracion:

- `POST /api/v1/usuarios`
- `GET /api/v1/estudiantes`
- `GET /api/v1/estudiantes/{id}`
- `POST /api/v1/estudiantes`
- `PUT /api/v1/estudiantes/{id}`
- `DELETE /api/v1/estudiantes/{id}`
- `GET /api/v1/grados`
- `POST /api/v1/grados`
- `PUT /api/v1/grados/{id}`
- `DELETE /api/v1/grados/{id}`
- `GET /api/v1/puntos-acceso`
- `POST /api/v1/puntos-acceso`
- `PUT /api/v1/puntos-acceso/{id}`
- `DELETE /api/v1/puntos-acceso/{id}`
- `GET /api/v1/acudientes`
- `POST /api/v1/acudientes`
- `PUT /api/v1/acudientes/{id}`
- `DELETE /api/v1/acudientes/{id}`

Operacion:

- `GET /api/v1/estudiantes/presentes`
- `POST /api/v1/registros-acceso/ingresos`
- `POST /api/v1/registros-acceso/salidas`
- `GET /api/v1/registros-acceso/estudiantes/{estudianteId}`
- `POST /api/v1/novedades`
- `PATCH /api/v1/novedades/{id}/estado`
- `GET /api/v1/dashboard/metricas`

Acudiente:

- `GET /api/v1/acudientes/me/estudiantes`
- `GET /api/v1/acudientes/me/notificaciones`
- `PATCH /api/v1/acudientes/me/notificaciones/{id}/leida`

## Flujo Git sugerido

Usar `main` para entregas estables, `dev` para integracion y ramas `feature/...` para cambios pequenos. Cada integrante debe hacer aportes reales que pueda explicar: una prueba, un endpoint, documentacion tecnica, una revision de Pull Request o una coleccion de Postman validada. No se deben hacer commits en nombre de otra persona.
