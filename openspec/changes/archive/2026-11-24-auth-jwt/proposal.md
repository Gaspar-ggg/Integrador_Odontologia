# Proposal

## Why

El sistema ya cuenta con el modelo de identidad (`Usuario`, `Rol`, repositorio y seed) pero no expone autenticación ni autorización por token. C-03 desbloquea el acceso seguro a la API para los cambios posteriores (catálogo, turnos, atención clínica) implementando sesión stateless con JWT y RBAC alineado al dominio odontológico.

## What Changes

- Agregar endpoint `POST /api/v1/auth/login` que autentica credenciales, valida BCrypt (costo >= 10), rechaza cuentas inactivas con HTTP 403 y emite JWT con claims `username`, `id`, `rol` y expiración <= 24h.
- Agregar endpoint `POST /api/v1/auth/register` público para alta de paciente: acepta solo `username`, `email`, `password`; asigna rol `PACIENTE` automáticamente; nunca acepta rol desde el cliente.
- Agregar endpoint `GET /api/v1/auth/me` autenticado que devuelve la identidad del usuario actual desde `SecurityContext`.
- Implementar infraestructura JWT: `JwtTokenProvider`, `JwtAuthenticationFilter`, `CustomUserDetailsService`, y configuración de `SecurityFilterChain` con sesión `STATELESS`, CSRF deshabilitado, CORS local para frontend y whitelist de rutas públicas.
- Manejar la trampa de `@SQLRestriction("activo = true")`: agregar consulta nativa en `UsuarioRepository` para recuperar usuarios inactivos por `username`, verificar `activo` explícitamente y lanzar excepción de dominio mapeada a 403 en `GlobalExceptionHandler`.
- Exponer authorities de Spring Security como `ROLE_` + `rol.name()` (ej. `ROLE_PACIENTE`) manteniendo el enum `Rol` sin prefijo.
- Agregar DTOs de request/response (`LoginRequest`, `RegisterRequest`, `AuthResponse`, `UserMeResponse`); prohibido exponer entidades JPA en controllers.
- Configurar JWT por entorno en `application.yml`: `${JWT_SECRET:...}` y `${JWT_EXPIRATION_MS:86400000}` con defaults solo para desarrollo, sin secretos hardcodeados en clases Java.
- Agregar dependencias JWT `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` versión `0.12.6` en `backend/build.gradle`, verificando compatibilidad con Jackson en Spring Boot 4.2.0 / Spring Security 7.
- Agregar cliente frontend mínimo: `frontend/src/api/auth.ts` y `frontend/src/utils/jwt.ts` para login/register/me, almacenamiento de token y header `Authorization: Bearer`.
- **Sin refresh token** en este cambio.

## Capabilities

### New Capabilities
- `auth-session`: Autenticación stateless con JWT, registro público de pacientes, endpoint de identidad autenticada, RBAC por authorities Spring Security, CORS local y manejo de cuentas inactivas.

### Modified Capabilities

## Impact

- **Backend código**: `backend/src/main/java/com/odontologia/config/SecurityConfig.java`, nuevo paquete `security/` (o clases equivalentes), `controller/` para auth, `dto/` auth, `service/` auth/user details, `repository/UsuarioRepository.java`, `exception/GlobalExceptionHandler.java` y nueva excepción de dominio para 403.
- **Backend config**: `backend/build.gradle` (dependencias jjwt), `backend/src/main/resources/application.yml` (propiedades JWT).
- **Frontend**: `frontend/src/api/auth.ts`, `frontend/src/utils/jwt.ts`; tipos alineados a DTOs.
- **APIs**: nuevos endpoints `/api/v1/auth/login`, `/api/v1/auth/register`, `/api/v1/auth/me`; cambios de política de acceso en `SecurityFilterChain`.
- **Seguridad**: define contrato de claims, expiración, manejo 401/403 y mapeo `ROLE_`.
- **Dependencias**: jjwt 0.12.6; requiere verificación de compatibilidad Jackson con Boot 4.2.0.
- **Sistemas afectados**: desbloquea C-04 y C-05; no incluye refresh token, recuperación de contraseña ni gestión de usuarios administrativos.
