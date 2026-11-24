# Tasks

## 1. Configuración y dependencias JWT

- [x] 1.1 Agregar dependencias `io.jsonwebtoken:jjwt-api:0.12.6`, `jjwt-impl:0.12.6` y `jjwt-jackson:0.12.6` en `backend/build.gradle` (impl/jackson como runtime) y verificar que `./gradlew dependencies --configuration runtimeClasspath` o `./gradlew compileJava` resuelven sin conflictos.
- [x] 1.2 Agregar propiedades JWT en `backend/src/main/resources/application.yml` con defaults de desarrollo `${JWT_SECRET:...}` y `${JWT_EXPIRATION_MS:86400000}`, y verificar que la aplicación levanta/parsea configuración en contexto de test sin secretos hardcodeados en clases Java.

## 2. Contratos DTO y manejo de errores de autenticación

- [x] 2.1 Crear DTOs de auth (`LoginRequest`, `RegisterRequest`, `AuthResponse`, `UserMeResponse`) con validaciones Jakarta apropiadas y sin exponer entidades JPA; verificar con tests de validación/serialización o compilación y revisión de contratos usados por controllers.
- [x] 2.2 Agregar excepción de dominio para cuenta deshabilitada/acceso prohibido y mapearla en `GlobalExceptionHandler` a HTTP 403 con `ErrorResponseDTO`; verificar con test de slice/unit del handler que el payload incluye `status=403`, `error`, `message`, `timestamp` y `path`.
- [x] 2.3 Agregar consulta nativa en `UsuarioRepository` para buscar usuario por username incluyendo inactivos (bypass de `@SQLRestriction`); verificar con test de repositorio/JPA que devuelve usuario inactivo mientras el finder derivado normal lo omite.

## 3. Infraestructura de seguridad JWT

- [x] 3.1 Implementar `JwtTokenProvider` para generar y validar tokens HS256 con claims `sub`, `id`, `rol`, `iat`, `exp`, leyendo secret/expiración desde configuración y aplicando tope <= 24h; verificar con tests unitarios de generación, validación, expiración y rechazo de token inválido.
- [x] 3.2 Implementar `CustomUserDetailsService` que cargue usuario por username, exponga authority `ROLE_` + `rol.name()` y soporte el flujo de login; verificar con tests unitarios que el usuario activo produce `UserDetails` con authority esperada y que credenciales/estado se mapean correctamente.
- [x] 3.3 Implementar `JwtAuthenticationFilter` para extraer `Authorization: Bearer`, validar token y poblar `SecurityContext`; verificar con tests de filtro/slice que token válido autentica, token inválido/ausente no autentica request protegida y no se rompe el flujo público.
- [x] 3.4 Actualizar `SecurityConfig` para `STATELESS`, CSRF off, CORS local, `AuthenticationManager`/encoder wiring, whitelist pública (`/api/v1/auth/login`, `/api/v1/auth/register`, `/api/health/**`) y filtro JWT antes de `UsernamePasswordAuthenticationFilter`; verificar con tests de seguridad que rutas públicas responden sin JWT y rutas no públicas exigen autenticación.

## 4. Endpoints de autenticación backend

- [x] 4.1 Implementar `POST /api/v1/auth/login` usando `AuthenticationManager`/servicio auth, devolviendo `AuthResponse{token,tipo,rol}` en éxito; verificar con test de integración/controller que credenciales válidas retornan 200 y token utilizable.
- [x] 4.2 Implementar el caso RN-AU-04 en login: si el usuario existe pero `activo=false`, responder 403 con error estructurado y sin emitir token; verificar con test de integración que credenciales correctas de usuario inactivo devuelven 403 (no 401).
- [x] 4.3 Implementar `POST /api/v1/auth/register` aceptando solo `username,email,password`, asignando rol `PACIENTE`, hasheando password con BCrypt y rechazando duplicados con 409; verificar con tests que el registro exitoso retorna 201 sin password/JPA entity, que no permite asignar rol privilegiado y que duplicados retornan 409.
- [x] 4.4 Implementar `GET /api/v1/auth/me` para devolver `UserMeResponse` del usuario autenticado desde `SecurityContext`; verificar con tests que con JWT válido retorna 200 con `id,username,email,rol,activo` y sin token/ inválido retorna 401.

## 5. Cliente frontend de autenticación

- [x] 5.1 Crear `frontend/src/utils/jwt.ts` con utilidades para guardar, leer, eliminar token y construir header `Authorization`; verificar con `npm run build` en `frontend` y, si hay tests/utilidades disponibles, cobertura básica de las funciones.
- [x] 5.2 Crear `frontend/src/api/auth.ts` con funciones tipadas `login`, `register` y `me` usando `fetch`, DTOs/interfaces TS alineadas al backend y manejo de errores HTTP; verificar con `npm run build` en `frontend` y revisión de que no usa `any`.

## 6. Verificación integral

- [x] 6.1 Ejecutar la suite backend relevante (`./gradlew test` o tests focalizados de auth + regresión necesaria) y verificar que pasan los casos clave: login 200, credenciales inválidas 401, usuario inactivo 403, register PACIENTE 201/409, me 200/401.
- [x] 6.2 Ejecutar `npm run build` en `frontend` y verificar que compila sin errores de TypeScript, confirmando que los contratos de `auth.ts`/`jwt.ts` son consistentes con el alcance de C-03.

## Workflow follow-up

- Revisar los artefactos del change con el equipo/usuario antes de iniciar implementación.
- Cuando se apruebe la implementación, iniciar apply con el skill `openspec-apply-change`.
- Tras completar implementación y verificación, archivar con `openspec-archive-change`.
