# Design

## Context

Ver `proposal.md` para motivación y alcance. El backend ya tiene Spring Boot 4.2.0, Spring Security, JPA, `Usuario`/`Rol`, `UsuarioRepository`, `DataInitializer` con BCrypt(10), y `SecurityConfig` stateless mínimo que solo permite `/api/health/**`. No hay infraestructura JWT, no hay endpoints de auth, no hay CORS configurado y no hay cliente auth en frontend. Las restricciones de dominio y técnicas acordadas provienen de la KB y de la exploración previa.

## Goals / Non-Goals

**Goals:**
- Implementar autenticación stateless JWT y autorización RBAC para C-03.
- Exponer `login`, `register` (PACIENTE) y `me` con contratos DTO claros.
- Garantizar el comportamiento 403 para cuentas inactivas pese a `@SQLRestriction`.
- Dejar configuración lista para frontend local y consumo con `Authorization: Bearer`.

**Non-Goals:**
- Refresh tokens, revocación de tokens, logout server-side o blacklist.
- Recuperación de contraseña, verificación de email, gestión administrativa de usuarios.
- Cambios en el modelo de roles o en el esquema de base de datos más allá de lo necesario para consultas/auth.
- Implementación de features de C-04/C-05.

## Decisions

### 1. Librería JWT: jjwt 0.12.6
- **Decisión**: Usar `io.jsonwebtoken:jjwt-api:0.12.6`, `jjwt-impl:0.12.6` (runtime) y `jjwt-jackson:0.12.6` (runtime).
- **Rationale**: Alineado a la restricción acordada; API simple para HS256, claims y validación de expiración.
- **Alternativa considerada**: Nimbus/JOSE de Spring Security. Se descarta por mayor ceremonia para este caso y porque la restricción ya fijó jjwt.
- **Nota de verificación**: Confirmar compatibilidad de `jjwt-jackson` con la versión de Jackson presente en Spring Boot 4.2.0 antes de cerrar la implementación.

### 2. Flujo de autenticación y carga de usuario
- **Decisión**: Crear `CustomUserDetailsService` que cargue `Usuario` por `username`. Para login, usar `AuthenticationManager` con `DaoAuthenticationProvider`/configuración equivalente en Security 7. El password se valida con `BCryptPasswordEncoder` existente.
- **Rationale**: Reutiliza el modelo de Spring Security, centraliza conversión de `Rol` a authority y evita lógica ad hoc en el controller.
- **Alternativa considerada**: Validación manual en service sin `AuthenticationManager`. Se descarta porque pierde integración estándar con SecurityContext y manejo de credenciales.

### 3. Cuentas inactivas y `@SQLRestriction`
- **Decisión**: Agregar en `UsuarioRepository` una consulta nativa `findByUsernameIncludingInactive` que seleccione desde `usuarios` sin pasar por el filtro de entidad. El flujo de login usará esta consulta para distinguir "no existe/credenciales inválidas" de "cuenta inactiva".
- **Rationale**: `@SQLRestriction("activo = true")` impide observar inactivos con queries derivadas; sin esta vía, RN-AU-04 devolvería 401.
- **Alternativa considerada**: Quitar `@SQLRestriction`. Se descarta por impacto transversal en el modelo y riesgo sobre soft delete.
- **Manejo de error**: Lanzar excepción de dominio específica (ej. `AccountDisabledException`/`ForbiddenException`) mapeada en `GlobalExceptionHandler` a HTTP 403 con `ErrorResponseDTO`.

### 4. Contrato JWT y claims
- **Decisión**: `JwtTokenProvider` generará tokens HS256 con `sub=username`, `id`, `rol`, `iat` y `exp`. La expiración efectiva se tomará de `JWT_EXPIRATION_MS` con tope máximo de 24h.
- **Rationale**: Cumple RN-AU-02 y la restricción acordada.
- **Alternativa considerada**: Poner solo `sub` y cargar rol desde DB en cada request. Se descarta para este alcance por simplicidad; el claim `rol` es suficiente y el rol se puede validar contra estado actual cuando haga falta en futuros cambios.

### 5. RBAC y authorities
- **Decisión**: Convertir `Rol.PACIENTE` -> authority `ROLE_PACIENTE` en `UserDetailsService`/helper. Mantener enum `Rol` sin prefijo.
- **Rationale**: Compatible con `hasRole('PACIENTE')` y con reglas de autorización por endpoint.
- **Alternativa considerada**: Renombrar enum a `ROLE_*`. Se descarta por impacto en persistencia/datos y por la restricción explícita.

### 6. Endpoints y DTOs
- **Decisión**: Controllers solo con DTOs.
  - `LoginRequest`: `username`, `password`.
  - `RegisterRequest`: `username`, `email`, `password` (sin rol).
  - `AuthResponse`: `token`, `tipo`, `rol`.
  - `UserMeResponse`: `id`, `username`, `email`, `rol`, `activo`.
- **Rationale**: Cumple regla dura de no exponer entidades JPA y fija contrato mínimo para frontend.
- **Alternativa considerada**: Reutilizar `Usuario` o devolver más campos. Se descarta por seguridad y acoplamiento.

### 7. Register
- **Decisión**: `POST /api/v1/auth/register` crea usuarios `PACIENTE` con password BCrypt. Valida unicidad de username/email antes de persistir y ante carrera/conflicto DB devuelve 409.
- **Rationale**: Cubre la ruta pública definida en KB y evita escalamiento de privilegios.
- **Alternativa considerada**: Dejar register fuera de C-03. Se descarta por decisión explícita de alcance del usuario.

### 8. SecurityFilterChain y CORS
- **Decisión**: Configurar `STATELESS`, CSRF off, CORS habilitado para orígenes locales de desarrollo, `JwtAuthenticationFilter` antes de `UsernamePasswordAuthenticationFilter`, y whitelist pública mínima: `/api/v1/auth/login`, `/api/v1/auth/register`, `/api/health/**` más recursos estáticos necesarios.
- **Rationale**: Permite auth frontend local sin abrir endpoints protegidos.
- **Alternativa considerada**: `permitAll()` amplio. Se descarta por riesgo de seguridad.

### 9. Frontend mínimo
- **Decisión**: `frontend/src/utils/jwt.ts` encapsula lectura/escritura/eliminación de token y helpers de header; `frontend/src/api/auth.ts` expone `login`, `register`, `me` usando `fetch` y DTOs/tipos TS.
- **Rationale**: Cumple alcance de CHANGES y separa concerns sin introducir framework.
- **Alternativa considerada**: Manejar token inline en cada llamada. Se descarta por duplicación.

## Risks / Trade-offs

- [Compatibilidad Jackson/Boot 4.2.0 con jjwt-jackson] → Verificar en build/test temprano; si hay conflicto, ajustar dependencia o serializador JSON de jjwt sin cambiar el contrato.
- [Consulta nativa bypassea `@SQLRestriction`] → Limitar su uso solo al flujo de autenticación/controlado; no exponerla como endpoint genérico.
- [403 vs 401 en cuentas inactivas] → Cubrir con test de integración específico; es el principal riesgo de regresión semántica.
- [Secret default de desarrollo] → Documentar como solo-dev; en entornos reales exigir `JWT_SECRET` fuerte por env.
- [Register público] → Validar input, normalizar email/username según política existente y aplicar rate limiting en futuro (fuera de alcance).
- [Token con claim `rol`] → Si el rol cambia, el token vigente conserva el rol anterior hasta expirar; aceptable en C-03, revisar en cambios futuros.

## Migration Plan

1. Agregar dependencias jjwt y propiedades JWT en `application.yml`.
2. Implementar DTOs, excepción 403 y handler.
3. Implementar `JwtTokenProvider`, `CustomUserDetailsService`, `JwtAuthenticationFilter`.
4. Actualizar `SecurityConfig` con CORS, whitelist, auth manager/provider y filtro JWT.
5. Implementar controller/service de auth (`login`, `register`, `me`).
6. Agregar cliente TS (`auth.ts`, `jwt.ts`).
7. Verificar con tests: login ok, credenciales inválidas 401, inactivo 403, register PACIENTE, me con/sin token, rutas protegidas.
8. **Rollback**: revertir change; no hay migración de datos destructiva. Usuarios creados por register quedan en DB como registros normales si ya se ejecutó en entorno compartido.

## Open Questions

- Ninguna que bloquee el alcance acordado. La compatibilidad Jackson/jjwt se trata como verificación técnica de implementación, no como ambigüedad de producto.

## UI Mockups

Design system revision: 1
Decision: no (no UI)
Date: 2026-11-24
