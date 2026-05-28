# Flujo GitHub del equipo

Este flujo esta pensado para integrantes con poca experiencia en GitHub y evita mezclar cambios directamente en `main`.

## Ramas

- `main`: version estable para entrega.
- `dev`: integracion del avance de la semana.
- `feature/backend-seguridad`: login, JWT, roles y pruebas de seguridad.
- `feature/backend-catalogos`: CRUD de grados, puntos de acceso y acudientes.
- `feature/backend-registros`: ingreso, salida, novedades y dashboard.
- `feature/docs-postman`: README, manual breve y coleccion de pruebas de Postman.

## Reglas de trabajo

1. Cada integrante trabaja desde su propia cuenta de GitHub.
2. Nadie hace commits usando el nombre o correo de otra persona.
3. El correo de autor del commit debe estar verificado en GitHub o usar el correo `noreply` de la cuenta; si no, el commit no cuenta en el perfil.
4. Cada Pull Request debe tener una descripcion corta: que cambio, como se probo y que endpoint afecta.
5. Antes de aprobar un Pull Request, otro integrante debe leerlo y ejecutar al menos una prueba.
6. Si alguien no programa una parte grande, puede aportar de forma real con pruebas en Postman, documentacion tecnica, screenshots de Swagger o revision de codigo.

## Comandos basicos

```powershell
git clone URL_DEL_REPOSITORIO
cd sigmae-fontana
git checkout dev
git pull
git checkout -b feature/docs-postman
```

Despues de editar:

```powershell
git status
git add .
git commit -m "docs: agrega flujo de pruebas de Postman"
git push -u origin feature/docs-postman
```

Luego se abre un Pull Request en GitHub desde la rama `feature/...` hacia `dev`.

## Tareas pequenas y defendibles

- Probar login y registrar capturas de Swagger o Postman.
- Agregar ejemplos de cuerpos JSON al README.
- Crear una prueba de API para un endpoint.
- Revisar que una ruta use el rol correcto.
- Documentar una entidad y sus relaciones.
- Verificar que `.gitignore` no suba `target/`, `.idea/` ni `.env`.

La sustentacion puede preguntar cualquier archivo. La meta no es aparentar commits, sino que cada integrante tenga una contribucion real y pueda explicarla.
