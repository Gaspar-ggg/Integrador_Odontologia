# Spec Delta

## Purpose

Define el comportamiento de autenticación stateless con JWT, registro público de pacientes y autorización por roles para la API del sistema odontológico.

## ADDED Requirements

### Requirement: Login con credenciales válidas
The system SHALL exponer `POST /api/v1/auth/login` para autenticar usuarios con `username` y `password`. Cuando las credenciales son válidas y la cuenta está activa, el sistema MUST validar la contraseña con BCrypt y responder HTTP 200 con un token JWT y metadatos de sesión.

#### Scenario: Login exitoso
- **WHEN** un cliente envía `POST /api/v1/auth/login` con `username` y `password` válidos de una cuenta activa
- **THEN** el sistema responde HTTP 200 con un payload que incluye `token`, `tipo` con valor `Bearer` y `rol` del usuario autenticado

### Requirement: Rechazo de credenciales inválidas
The system SHALL rechazar intentos de login con credenciales incorrectas sin revelar si el usuario existe.

#### Scenario: Credenciales inválidas
- **WHEN** un cliente envía `POST /api/v1/auth/login` con `username` o `password` incorrectos
- **THEN** el sistema responde HTTP 401 con un error estructurado y no emite un token JWT

### Requirement: Rechazo de cuenta inactiva
The system SHALL impedir la autenticación de usuarios inactivos, incluso si las credenciales son correctas, y MUST responder HTTP 403.

#### Scenario: Cuenta deshabilitada intenta autenticarse
- **WHEN** un cliente envía `POST /api/v1/auth/login` con credenciales correctas de un usuario cuyo `activo` es `false`
- **THEN** el sistema responde HTTP 403 con un error estructurado que indica cuenta deshabilitada y no emite un token JWT

### Requirement: Registro público de paciente
The system SHALL exponer `POST /api/v1/auth/register` para registrar pacientes. El request MUST aceptar solo `username`, `email` y `password`; el sistema MUST asignar el rol `PACIENTE` automáticamente y MUST ignorar/rechazar cualquier intento de asignar otro rol desde el cliente.

#### Scenario: Registro exitoso de paciente
- **WHEN** un cliente envía `POST /api/v1/auth/register` con `username`, `email` y `password` válidos y no existentes
- **THEN** el sistema crea el usuario con rol `PACIENTE`, persiste la contraseña con BCrypt costo >= 10 y responde HTTP 201 con datos de identidad sin exponer la contraseña ni la entidad JPA

#### Scenario: Registro duplicado
- **WHEN** un cliente envía `POST /api/v1/auth/register` con `username` o `email` ya existente
- **THEN** el sistema responde HTTP 409 con un error estructurado y no crea un usuario duplicado

### Requirement: Identidad autenticada actual
The system SHALL exponer `GET /api/v1/auth/me` para devolver la identidad del usuario autenticado a partir del contexto de seguridad.

#### Scenario: Consulta de identidad con token válido
- **WHEN** un cliente autenticado envía `GET /api/v1/auth/me` con un JWT válido en `Authorization: Bearer <token>`
- **THEN** el sistema responde HTTP 200 con datos de identidad del usuario, incluyendo al menos `id`, `username`, `email`, `rol` y `activo`

#### Scenario: Consulta de identidad sin autenticación válida
- **WHEN** un cliente envía `GET /api/v1/auth/me` sin token o con token inválido/expirado
- **THEN** el sistema responde HTTP 401 y no expone datos de identidad

### Requirement: Contrato de token JWT
The system SHALL emitir tokens JWT firmados para sesiones stateless. El token MUST incluir claims `sub` (username), `id` y `rol`, y MUST tener expiración no mayor a 24 horas.

#### Scenario: Token emitido contiene claims requeridos
- **WHEN** el sistema emite un token tras login exitoso
- **THEN** el token incluye `sub`, `id`, `rol` y una expiración `exp` menor o igual a 24 horas desde su emisión

#### Scenario: Token expirado o inválido es rechazado
- **WHEN** un cliente accede a un endpoint protegido con un token expirado, malformado o con firma inválida
- **THEN** el sistema rechaza la petición con HTTP 401

### Requirement: Autorización por roles RBAC
The system SHALL mapear el rol de dominio a authorities de Spring Security con el formato `ROLE_` + `rol.name()`.

#### Scenario: Authority derivada del rol
- **WHEN** un usuario con rol `PACIENTE` se autentica correctamente
- **THEN** el contexto de seguridad expone la authority `ROLE_PACIENTE` para decisiones de autorización

#### Scenario: Acceso denegado por rol insuficiente
- **WHEN** un usuario autenticado accede a un endpoint que requiere una authority distinta a las que posee
- **THEN** el sistema responde HTTP 403

### Requirement: Política de rutas y CORS para autenticación
The system SHALL configurar la API como stateless, deshabilitar CSRF, habilitar CORS para el origen local del frontend y permitir acceso público solo a rutas explícitamente definidas.

#### Scenario: Rutas públicas accesibles sin token
- **WHEN** un cliente no autenticado llama a `POST /api/v1/auth/login`, `POST /api/v1/auth/register` o `GET /api/health/**`
- **THEN** el sistema permite la petición según la política pública configurada sin exigir JWT previo

#### Scenario: Rutas protegidas exigen autenticación
- **WHEN** un cliente no autenticado llama a un endpoint no público distinto de los permitidos
- **THEN** el sistema responde HTTP 401

### Requirement: Configuración externa de JWT
The system SHALL obtener la clave y expiración JWT desde configuración externa, usando defaults solo para desarrollo.

#### Scenario: Configuración por variables de entorno
- **WHEN** la aplicación inicia con `JWT_SECRET` y/o `JWT_EXPIRATION_MS` definidos
- **THEN** el sistema usa esos valores para firmar y validar tokens; si no están definidos, usa defaults de desarrollo y la expiración efectiva no supera 24 horas
