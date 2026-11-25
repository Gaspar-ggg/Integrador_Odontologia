# Proposal

## Why

C-03 dejó lista la autenticación JWT y el mapeo RBAC, pero el sistema todavía no expone el catálogo de profesionales ni la gestión administrativa de odontólogos, que es prerequisite directa de C-06 (turnos) y C-08 (evoluciones clínicas). Además, la configuración actual tiene dos brechas verificadas: `@PreAuthorize` no surte efecto porque falta `@EnableMethodSecurity`, y `GET /api/v1/public/odontologos` —declarado público en la KB— respondería 401. C-04 cierra esas brechas y entrega el dominio `Profesionales`.

## What Changes

- Agregar la entidad JPA `Odontologo` (`usuario_id` FK UNIQUE, `matricula` UNIQUE, `especialidad`, `activo`) con soft delete mediante `@SQLDelete` y `@SQLRestriction`, heredando de `AuditableEntity`.
- Agregar `OdontologoRepository` con búsqueda por matrícula, especialidad y estado activo, más consulta nativa para inactivos.
- Agregar DTOs: `OdontologoRequestDTO` (con `password` obligatorio y validado), `OdontologoResponseDTO` y `OdontologoPublicDTO` (sólo `id`, `nombre`, `apellido`, `matricula`, `especialidad`; sin `username` ni `email`).
- Agregar `OdontologoService`: alta transaccional que crea el `Usuario` con rol `ODONTOLOGO` y su `Odontologo` en una única transacción; valida duplicados de `username`/`email` (incluyendo inactivos) y de matrícula, respondiendo 409 con `ConflictException`.
- Agregar endpoints administrativos protegidos con `@PreAuthorize("hasRole('ADMIN')")` en el controller: `POST /api/v1/admin/odontologos`, `PUT /api/v1/admin/odontologos/{id}` y `DELETE /api/v1/admin/odontologos/{id}` (baja lógica del odontólogo **y** de su usuario asociado en la misma transacción).
- Agregar `GET /api/v1/public/odontologos` sin autenticación y sin filtros, usando `OdontologoPublicDTO`.
- Habilitar `@EnableMethodSecurity` en `SecurityConfig` y ampliar la whitelist sólo con `HttpMethod.GET` para la ruta pública.
- Agregar en `GlobalExceptionHandler` un handler de `AccessDeniedException` que devuelva 403 con `ErrorResponseDTO` (RN-EX-02), evitando que el catch-all genérico responda 500.
- Agregar seed idempotente en `DataInitializer`: un `Usuario` `ODONTOLOGO` con password BCrypt(10) y su `Odontologo` asociado.
- Actualizar `docs/api-requests.http` con el campo `password` en el alta y ejemplos de `PUT` y `DELETE`.
- Cubrir el change con tests TDD al 100%: repositorio (H2), servicio, controller admin por rol y acceso público sin token.

No hay cambios breaking: los endpoints son nuevos y el contrato de alta se define por primera vez en este change.

## Capabilities

### New Capabilities

- `profesionales`: modelo y contratos del dominio de odontólogos — alta administrativa vinculando una cuenta de usuario, modificación, baja lógica preservando integridad histórica y catálogo público sin autenticación.

### Modified Capabilities

Ninguna. Las capacidades `auth-session`, `user-identity` y `system-foundation` conservan sus requerimientos: `auth-session` ya define el mapeo `ROLE_` y el principio de "rutas públicas explícitas", y `user-identity` ya define soft delete y respuestas de error uniformes. El nuevo comportamiento se expresa en la capacidad nueva.

## Impact

- **Backend código**: nuevos `model/Odontologo.java`, `repository/OdontologoRepository.java`, `dto/Odontologo*DTO.java`, `service/OdontologoService.java`, `controller/OdontologoController.java`; modificaciones en `config/SecurityConfig.java`, `config/DataInitializer.java` y `exception/GlobalExceptionHandler.java`.
- **Backend tests**: nuevos tests de repositorio, servicio, controller e integración pública/RBAC sobre H2; patrón de slices `@WebMvcTest` de `AuthControllerTest`.
- **APIs**: `POST`/`PUT`/`DELETE /api/v1/admin/odontologos` y `GET /api/v1/public/odontologos`.
- **Seguridad**: activación de method security, nueva ruta pública acotada a GET y mapeo explícito de denegación a 403.
- **Docs / entregable**: `docs/api-requests.http` y `CHANGES.md` (marca `[x]` al archivar).
- **Dependencias**: ninguna nueva; se reutilizan Spring Security, JPA, Jakarta Validation, Lombok, BCrypt(10), H2 y las excepciones de dominio existentes.
- **Consumidores futuros**: desbloquea C-06 (`Turno.odontologo`, disponibilidad) y C-08 (`EvolucionClinica.odontologo_id`). Este change es no visual, por lo que no incluye UI ni mockups.
