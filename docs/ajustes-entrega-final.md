# Ajustes de cierre para entrega final

Este archivo registra ajustes hechos sobre el repositorio para mantener coherencia entre la guia del proyecto, la implementacion y la sustentacion.

## Frontend

El documento final menciona React + Vite como alternativa de frontend. La version entregada en el repositorio usa frontend estatico servido por Spring Boot desde `src/main/resources/static`.

Esta decision mantiene el elevador de frontend porque:

- Consume endpoints reales del backend.
- Usa JWT real despues del login.
- Permite probar talanquera, dashboard de coordinador, vista de acudiente y gestion administrativa.
- No simula respuestas principales del sistema.

Para ejecutar no se usa `npm install`, `npm run dev` ni `http://localhost:5173`. La URL correcta es:

```text
http://localhost:8080/
```

## Seguridad

El backend diferencia:

- `401 Unauthorized`: token ausente, invalido o expirado.
- `403 Forbidden`: usuario autenticado sin rol suficiente.

Esto alinea la API con la rubrica del proyecto y con el comportamiento esperado de Spring Security + JWT.

## Relacion estudiante-acudiente

La guia pide una relacion muchos-a-muchos. En el codigo se implementa mediante la entidad intermedia `EstudianteAcudiente`, en lugar de una anotacion directa `@ManyToMany`.

La razon tecnica es que la relacion necesita atributos propios:

- `parentesco`
- `responsablePrincipal`

Por eso el modelo usa dos relaciones `@ManyToOne` sobre una tabla intermedia con clave compuesta. Esta es una forma mas flexible y defendible que un `@ManyToMany` directo cuando la relacion tiene datos de negocio.
