# Design

## Context

Ver `proposal.md` para motivación y `specs/profesionales/spec.md` para el contrato. El backend es Spring Boot 4.2.0 / Java 17 con Spring Security 7 vía `spring-boot-starter-security`. `SecurityConfig` tiene `@EnableWebSecurity`, filtro JWT antes de `UsernamePasswordAuthenticationFilter`, `SessionCreationPolicy.STATELESS` y whitelist sólo de `/api/health/**`, login y register. `JwtAuthenticationFilter` y `CustomUserDetailsService` emiten authorities `ROLE_` + `rol.name()`, compatibles con `hasRole`. No hay `@EnableMethodSecurity`, por lo que `@PreAuthorize` hoy es inerte. `Usuario` ya usa `@SQLDelete`/`@SQLRestriction` sobre `AuditableEntity` (que aporta `activo`, `fecha_creacion`, `fecha_modificacion` y `desactivar()`), y `UsuarioRepository` expone consultas nativas `*IncludingInactive` para sortear el filtro de entidad. `GlobalExceptionHandler` tiene un `@ExceptionHandler(Exception.class)` que devuelve 500. `DataInitializer` siembra ADMIN y RECEPCIÓN con BCrypt(10). Los tests usan H2 con `ddl-auto: create-drop`; no hay scripts SQL ni Flyway.

## Goals / Non-Goals

**Goals:**
- Hacer efectiva la autorización por método para los endpoints administrativos y devolver 403 estructurado en la denegación.
- Crear la cuenta `ODONTOLOGO` y el profesional en una sola transacción, con validación de duplicados que incluya cuentas inactivas.
- Publicar el catálogo sin token con un payload que no filtre datos de la cuenta.
- Aplicar baja lógica simultánea de profesional y cuenta, conservando las filas para integridad histórica.
- Dejar el change cubierto por tests TDD (rojo→verde) al 100% de los escenarios de la spec.

**Non-Goals:**
- Filtros, búsqueda o paginación en el catálogo público (quedan para una iteración posterior).
- Reactivación de profesionales dados de baja, cambio de contraseña, recuperación ni gestión administrativa genérica de usuarios.
- Frontend, mockups ni cambios de `design-system/` (C-04 es no visual).
- Migraciones Flyway/Liquibase: se mantiene `ddl-auto` como en los changes anteriores.
- Actualizar `fecha_modificacion` durante el soft delete (se acepta el mismo comportamiento que `Usuario`).

## Decisions

### 1. Activar method security en `SecurityConfig`
Agregar `@EnableMethodSecurity` sobre la clase de configuración existente. `prePostEnabled` es `true` por defecto, así que no hacen falta parámetros.
- **Rationale**: Spring Boot Starter Security no activa autorización por método por defecto; sin esto, `@PreAuthorize("hasRole('ADMIN')")` se ignora y cualquier usuario autenticado podría crear odontólogos. Ponerlo en `SecurityConfig` hace que los slices `@WebMvcTest` que ya hacen `@Import(SecurityConfig.class)` hereden el comportamiento. C-08 también se beneficia.
- **Alternativa considerada**: reglas URL `.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")`. Descartada: CHANGES pide `@PreAuthorize` en el controller y la regla URL no protege llamadas entre beans.

### 2. `@PreAuthorize("hasRole('ADMIN')")` en el controller administrativo
- **Rationale**: decisión acordada; mantiene la política de acceso junto al contrato HTTP y es verificable con tests MockMvc/`@WithMockUser`.
- **Alternativa considerada**: anotar el servicio. Más robusto frente a llamadas internas, pero cambia el punto de aplicación previsto. Si en el futuro el alta se invoca desde otro flujo, se reevaluará.

### 3. Handler explícito de `AccessDeniedException` → 403
Agregar `@ExceptionHandler(AccessDeniedException.class)` en `GlobalExceptionHandler` construyendo `ErrorResponseDTO` con `HttpStatus.FORBIDDEN`.
- **Rationale**: el `@ExceptionHandler(Exception.class)` existente (`handleGenericException`) captura `AccessDeniedException` en el `DispatcherServlet` antes de que `ExceptionTranslationFilter` lo traduzca, y hoy respondería 500. El handler explícito garantiza RN-EX-02 y el escenario "Acceso denegado por rol insuficiente" de `auth-session`.
- **Alternativa considerada**: configurar `exceptionHandling().accessDeniedHandler(...)`. No alcanza, porque el `@RestControllerAdvice` ya resolvió la excepción. Quitar el catch-all global se descarta por riesgo de regresión en otros changes.

### 4. Whitelist pública acotada a un verbo
`.requestMatchers(HttpMethod.GET, "/api/v1/public/odontologos").permitAll()`.
- **Rationale**: la KB declara la ruta pública; `HttpMethod.GET` evita abrir accidentalmente otros verbos. El filtro JWT ignora tokens inválidos en esta ruta, por lo que el acceso sin token funciona sin cambios adicionales.
- **Alternativa considerada**: `permitAll()` sobre `/api/v1/public/**`. Descartada por ampliar la superficie pública más allá de lo acordado.

### 5. Password obligatorio en el contrato de alta
`OdontologoRequestDTO` incluye `password` con `@NotBlank`/`@Size`; se hashea con el bean `PasswordEncoder` existente (BCrypt costo 10) y se asigna `Rol.ODONTOLOGO`. `docs/api-requests.http` se actualiza con el campo y con ejemplos de `PUT`/`DELETE`.
- **Rationale**: `password_hash` es `NOT NULL` y no existe otro mecanismo para crear cuentas con rol de profesional; el contrato explícito evita credenciales temporales sin política de rotación.
- **Alternativa considerada**: generar una contraseña temporal y devolverla una sola vez. Descartada porque require cambio obligatorio en primer login (fuera de alcance). También se descartó pedir un `usuarioId` existente: no hay endpoint administrativo de alta de usuarios.

### 6. Transacción única y validación de duplicados
`OdontologoService` concentra el alta, la modificación y la baja bajo `@Transactional`. Antes de persistir, verifica `findByUsernameIncludingInactive`, `findByEmailIncludingInactive` y `existsByMatricula`, lanzando `ConflictException` → 409. Las columnas `username`, `email`, `matricula` y `usuario_id` conservan `unique = true` como última línea de defensa; `DataIntegrityViolationException` ya está mapeado a 409.
- **Rationale**: evita cuentas huérfanas ante un fallo parcial y cubre el caso de cuentas inactivas que `@SQLRestriction` oculta a los repositorios derivados.
- **Alternativa considerada**: validar sólo la UNIQUE de BD. Descartada: daría 409 con mensaje genérico y podría dejar la cuenta creada si la transacción no se controla.

### 7. Soft delete espejo de `Usuario`
`Odontologo` extiende `AuditableEntity` y replica `@SQLDelete(sql = "UPDATE odontologos SET activo = false WHERE id = ?")` y `@SQLRestriction("activo = true")`. El `DELETE` admin llama a `desactivar()` del odontólogo y del usuario vinculado dentro de la misma transacción (sin `repository.delete()` físico).
- **Rationale**: cumple RN-EX-01 y RN-AU-04; dejar la cuenta activa permitiría login de un profesional dado de baja. El `@SQLRestriction` excluye automáticamente a los inactivos del catálogo.
- **Alternativa considerada**: desactivar sólo el odontólogo. Descartada por la cuenta con acceso vigente.
- **Trade-off**: las relaciones `@ManyToOne` desde `Turno`/`EvolucionClinica` no resolverán al odontólogo inactivo. Se documenta y se revisa en C-06/C-08; si se requiere el nombre histórico, se evaluará denormalizarlo o usar consulta nativa (fuera de este change).

### 8. Separación de DTOs
`OdontologoRequestDTO` (entrada admin, con credenciales), `OdontologoResponseDTO` (salida admin, sin password) y `OdontologoPublicDTO` (`id`, `nombre`, `apellido`, `matricula`, `especialidad`). Ningún controller expone entidades JPA.
- **Rationale**: regla dura del proyecto; además evita filtrar `username`/`email` en el catálogo abierto.
- **Alternativa considerada**: reutilizar `OdontologoResponseDTO` en el catálogo. Descartada por privacidad.

### 9. Seed idempotente
`DataInitializer` agrega un usuario `ODONTOLOGO` con password conocida hasheada y su `Odontologo`, protegido por `existsByUsername` y `existsByMatricula`.
- **Rationale**: consistente con el seed ADMIN/RECEPCIÓN existente y con la KB (`04_modelo_de_datos.md`, seed inicial).
- **Alternativa considerada**: script SQL separado. Descartada: el repo no tiene migraciones y el patrón vigente es `CommandLineRunner`.

### 10. TDD y estrategia de tests
Cada escenario de la spec se escribe primero como test fallido y luego se implementa. Repositorio sobre H2; slices `@WebMvcTest` con `@Import({SecurityConfig.class, GlobalExceptionHandler.class})` y `@MockitoBean JwtAuthenticationFilter` (patrón de `AuthControllerTest`); `@WithMockUser(roles = "ADMIN"/"PACIENTE")` para RBAC; test de integración con MockMvc para catálogo sin token y admin con JWT real.
- **Rationale**: la mayoría de los defectos de este change son de configuración (method security, whitelist, catch-all), y sólo se detectan si el test ejercita la cadena completa de seguridad.
- **Alternativa considerada**: tests de servicio únicamente. Descartada: daría falsos positivos sobre `@PreAuthorize` y sobre la ruta pública.

## Risks / Trade-offs

- [Method security inactivo por configuración faltante] → test explícito de `PACIENTE` recibiendo 403 en los tres endpoints admin.
- [Denegación convertida en 500 por el catch-all] → handler de `AccessDeniedException` + aserción de `$.status == 403` y `ErrorResponseDTO`.
- [Ruta pública devolviendo 401] → test MockMvc sin cabecera `Authorization` esperando 200.
- [`@SQLRestriction` oculta relaciones históricas] → documentar el límite; verificar en C-06/C-08 antes de mostrar nombre de profesional en turnos/evoluciones antiguas.
- [`@SQLDelete` no actualiza `fecha_modificacion`] → aceptado por consistencia con `Usuario`; si se quiere auditoría de baja, se revisará en un change futuro.
- [UNIQUE dependiente de `ddl-auto` sin migraciones] → declarar constraints en la entidad y validar colisiones en tests H2; Flyway queda diferido.
- [Seed nuevo afecta tests `@SpringBootTest`] → usar datos de prueba con nombres distintos y verificar la suite completa de auth.
- [Rol cacheado en el JWT de C-03] → no se agrava: la autorización de este change se basa en la authority del token, igual que C-03; el cambio de rol en caliente queda fuera de alcance.
- [Activar `@EnableMethodSecurity` globalmente impacta C-08] → efecto positivo, pero la suite de auth existente debe seguir verde como regresión.

## Migration Plan

1. Escribir los tests fallidos de configuración de seguridad y DTOs (TDD rojo).
2. Agregar entidad, repositorio, DTOs y servicio con la transacción atómica.
3. Agregar controller con `@PreAuthorize` y el endpoint público.
4. Ajustar `SecurityConfig` y `GlobalExceptionHandler`.
5. Actualizar `DataInitializer` y `docs/api-requests.http`.
6. Ejecutar `./gradlew test` sobre H2 y verificar que la suite de auth no regresó.
7. **Rollback**: revertir el change; no hay migración destructiva. Las filas creadas por el seed o por altas quedan como datos normales si ya se ejecutó en un entorno compartido.

## Open Questions

- ¿Debe C-06/C-08 denormalizar el nombre del odontólogo en `Turno`/`EvolucionClinica` para conservar la razón social histórica tras una baja? No afecta el contrato ni las tareas de C-04; se decide en esos changes.

## UI Mockups

Design system revision: 1
Decision: no (no UI)
Date: 2026-11-24
