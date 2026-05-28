# Seguridad operativa

Esta guia resume controles minimos para ejecutar SIGMAE Fontana sin exponer credenciales ni debilitar la autenticacion.

## Variables sensibles

- Definir `APP_JWT_SECRET` como variable de entorno en cada ambiente.
- Usar un secreto de al menos 256 bits y distinto para desarrollo, pruebas y produccion.
- No publicar contrasenas reales de base de datos en `application.yml`, capturas, tickets o documentacion.
- Rotar el secreto JWT si se sospecha que fue compartido o subido por error.

## Autenticacion y sesiones

- Mantener la expiracion del token JWT en el menor tiempo compatible con la operacion.
- Evitar usuarios compartidos; cada persona debe operar con una cuenta propia.
- Cambiar las contrasenas de usuarios semilla antes de usar el sistema fuera de un ambiente local.
- Bloquear o eliminar usuarios que ya no hagan parte del equipo.

## Autorizacion por roles

- Revisar que cada endpoint nuevo tenga un rol permitido de forma explicita.
- Validar los flujos con usuarios `ADMINISTRADOR`, `COORDINADOR`, `PORTERIA` y `ACUDIENTE`.
- No devolver datos de otros acudientes desde endpoints de autoservicio.
- Registrar en pruebas cualquier endpoint que cambie permisos o exponga datos personales.

## Datos y despliegue

- Usar HTTPS delante de la API en ambientes compartidos o productivos.
- Restringir el acceso a PostgreSQL por red, usuario y contrasena.
- Desactivar datos semilla con `APP_SEED_ENABLED=false` en produccion.
- Revisar logs antes de compartirlos para evitar exponer tokens, correos o documentos.

## Checklist antes de publicar

- `APP_JWT_SECRET` configurado fuera del repositorio.
- Usuarios semilla desactivados o con contrasenas cambiadas.
- Permisos probados por rol.
- Base de datos no accesible publicamente.
- Logs revisados sin tokens ni credenciales.
