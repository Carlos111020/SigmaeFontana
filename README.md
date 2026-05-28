# SIGMAE Fontana

Backend Spring Boot y frontend estatico para el control de ingreso, permanencia y salida de estudiantes del Gimnasio Campestre La Fontana.

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
- PostgreSQL.
- Spring Security 6 + JWT.
- Bean Validation.
- MapStruct.
- SpringDoc OpenAPI / Swagger UI.
- Maven Wrapper.
- Frontend estatico servido por Spring Boot.

## Arquitectura

- `controller`: endpoints REST versionados con prefijo `/api/v1`.
- `service` y `service.impl`: reglas de negocio y transacciones.
- `repository`: acceso a datos con Spring Data JPA.
- `entity`: modelo JPA del diseno entidad-relacion.
- `dto`: entradas y salidas de la API. No se exponen entidades JPA.
- `mapper`: conversion de entidades a DTO usando MapStruct.
- `security`: autenticacion JWT, BCrypt y autorizacion por roles.
- `exception`: respuestas de error estandarizadas.
- `static`: interfaz web de talanquera, dashboard coordinador y vista acudiente.

## Ejecucion Local

Crear la base de datos en PostgreSQL:

```sql
CREATE DATABASE sigmae_fontana;
```

Configurar variables de entorno si la instalacion no usa los valores por defecto:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/sigmae_fontana"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
$env:APP_JWT_SECRET="SIGMAE_FONTANA_SECRET_KEY_FOR_JWT_2026_MINIMUM_256_BITS"
$env:APP_JWT_EXPIRATION_MINUTES="480"
$env:APP_SEED_ENABLED="true"
```

Si PostgreSQL esta en otro puerto, cambie la URL. En la maquina de desarrollo usada para esta validacion el servicio local escuchaba en `8081`, por lo que la URL seria:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:8081/sigmae_fontana"
```

Ejecutar la aplicacion:

```powershell
.\mvnw.cmd spring-boot:run
```

URLs locales:

- Frontend: `http://localhost:8080/`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Pruebas

Las pruebas automatizadas usan H2 en modo PostgreSQL para ser rapidas y no depender de una instalacion local:

```powershell
.\mvnw.cmd test
```

Para validar contra PostgreSQL real, primero levante la aplicacion apuntando a PostgreSQL y luego ejecute:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\validate-postgres-flow.ps1 -BaseUrl http://localhost:8080
```

Ese script prueba el flujo completo: login, creacion de estudiante, creacion de acudiente, asociacion, ingreso por talanquera, salida por talanquera, novedad, dashboard coordinador y consulta del acudiente.

## Datos Semilla

Con una base vacia y `APP_SEED_ENABLED=true`, la aplicacion crea usuarios, grados, punto de acceso, jornada y 5 estudiantes de prueba que coinciden con los carnets del frontend.

Usuarios:

| Rol | Correo | Password |
| --- | --- | --- |
| ADMINISTRADOR | admin@sigmae.edu.co | Admin123* |
| COORDINADOR | coordinador@sigmae.edu.co | Coord123* |
| PORTERIA | porteria@sigmae.edu.co | Porteria123* |
| ACUDIENTE | acudiente@sigmae.edu.co | Acudiente123* |

Carnets de prueba:

| Carnet | Estudiante | Grado | Acudiente |
| --- | --- | --- | --- |
| EST-001 | Sofia Gomez | 6A | Laura Gomez |
| EST-002 | Mateo Rojas | 7B | Andres Rojas |
| EST-003 | Valentina Perez | 8A | Claudia Perez |
| EST-004 | Juan Martinez | 9B | Ricardo Martinez |
| EST-005 | Isabella Torres | 10A | Patricia Torres |

## Endpoints Principales

Autenticacion:

- `POST /api/v1/auth/login`

Usuarios:

- `POST /api/v1/usuarios`
- `GET /api/v1/usuarios`
- `GET /api/v1/usuarios/{id}`
- `PUT /api/v1/usuarios/{id}`
- `DELETE /api/v1/usuarios/{id}`

Catalogos y personas:

- `GET /api/v1/grados`
- `POST /api/v1/grados`
- `PUT /api/v1/grados/{id}`
- `DELETE /api/v1/grados/{id}`
- `GET /api/v1/puntos-acceso`
- `POST /api/v1/puntos-acceso`
- `PUT /api/v1/puntos-acceso/{id}`
- `DELETE /api/v1/puntos-acceso/{id}`
- `GET /api/v1/estudiantes`
- `GET /api/v1/estudiantes/{id}`
- `POST /api/v1/estudiantes`
- `PUT /api/v1/estudiantes/{id}`
- `DELETE /api/v1/estudiantes/{id}`
- `POST /api/v1/estudiantes/{id}/acudientes`
- `GET /api/v1/acudientes`
- `POST /api/v1/acudientes`
- `PUT /api/v1/acudientes/{id}`
- `DELETE /api/v1/acudientes/{id}`

Operacion:

- `GET /api/v1/estudiantes/presentes`
- `POST /api/v1/talanquera/ingresos`
- `POST /api/v1/talanquera/salidas`
- `POST /api/v1/registros-acceso/ingresos`
- `POST /api/v1/registros-acceso/salidas`
- `GET /api/v1/registros-acceso`
- `GET /api/v1/registros-acceso/estudiantes/{estudianteId}`
- `POST /api/v1/novedades`
- `GET /api/v1/novedades`
- `PATCH /api/v1/novedades/{id}/estado`
- `GET /api/v1/dashboard/metricas`

Acudiente:

- `GET /api/v1/acudientes/me/estudiantes`
- `GET /api/v1/acudientes/me/notificaciones`
- `PATCH /api/v1/acudientes/me/notificaciones/{id}/leida`

## Coleccion Postman

Los archivos para sustentacion estan en `docs`:

- `docs/SIGMAE-Fontana.postman_collection.json`
- `docs/SIGMAE-Fontana.postman_environment.json`

Importe ambos en Postman, seleccione el ambiente `SIGMAE Fontana Local` y ejecute las carpetas en orden. Para repetir toda la coleccion desde cero, borre la variable `runSuffix` del ambiente o use una base nueva.

## Seguridad

La guia de controles minimos para JWT, credenciales, roles y despliegue esta en `docs/seguridad-operativa.md`.

## Flujo Git Sugerido

Usar `main` para entregas estables, `dev` para integracion y ramas `feature/...` para cambios pequenos. Cada integrante debe hacer aportes reales que pueda explicar: una prueba, un endpoint, documentacion tecnica, una revision de Pull Request o una coleccion Postman validada. No se deben hacer commits en nombre de otra persona.
