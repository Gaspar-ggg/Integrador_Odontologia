# profesionales Specification

## Purpose

Define el contrato observable del dominio de profesionales: alta y mantenimiento administrativo de odontólogos vinculados a una cuenta de usuario, baja lógica que preserva el historial clínico y catálogo público accesible sin autenticación.

## Requirements

### Requirement: Alta administrativa de odontólogo con cuenta de usuario

The system SHALL exponer `POST /api/v1/admin/odontologos` para dar de alta profesionales. El request MUST incluir `nombre`, `apellido`, `matricula`, `especialidad`, `username`, `email` y `password` válidos. El sistema MUST almacenar la contraseña con BCrypt de costo >= 10, asignar el rol `ODONTOLOGO` y crear la cuenta y el profesional de forma atómica. La respuesta MUST ser HTTP 201 y MUST NOT exponer la contraseña.

#### Scenario: Alta exitosa vincula la cuenta de usuario

- **WHEN** un usuario con rol `ADMIN` envía `POST /api/v1/admin/odontologos` con todos los campos obligatorios válidos y no duplicados
- **THEN** el sistema responde HTTP 201 con los datos del profesional, crea una cuenta con rol `ODONTOLOGO` vinculada y no incluye la contraseña en la respuesta

#### Scenario: Password ausente o inválido rechaza el alta

- **WHEN** un `ADMIN` envía el alta sin `password` o con un valor que no supera la validación
- **THEN** el sistema responde HTTP 400 con `ErrorResponseDTO` y no crea ni la cuenta ni el profesional

### Requirement: Rechazo de profesionales duplicados

The system SHALL rechazar el alta cuando la matrícula ya exista o cuando el `username` o `email` ya esté registrado, incluso si la cuenta afectada está inactiva. El sistema MUST responder HTTP 409 con `ErrorResponseDTO` y MUST NOT persistir registros parciales.

#### Scenario: Matrícula duplicada

- **WHEN** un `ADMIN` envía un alta con una `matricula` ya existente
- **THEN** el sistema responde HTTP 409 con `ErrorResponseDTO` y no crea la cuenta ni el profesional

#### Scenario: Username o email duplicado de cuenta inactiva

- **WHEN** un `ADMIN` envía un alta con un `username` o `email` perteneciente a una cuenta dada de baja
- **THEN** el sistema responde HTTP 409 con `ErrorResponseDTO` y no crea la cuenta ni el profesional

### Requirement: Modificación de datos profesionales

The system SHALL exponer `PUT /api/v1/admin/odontologos/{id}` para actualizar `nombre`, `apellido`, `matricula` y `especialidad` de un profesional activo. El sistema MUST responder HTTP 200 con los datos actualizados, HTTP 404 si el identificador no existe y HTTP 409 si la nueva matrícula pertenece a otro profesional.

#### Scenario: Modificación exitosa

- **WHEN** un `ADMIN` envía `PUT /api/v1/admin/odontologos/{id}` con datos válidos de un profesional existente
- **THEN** el sistema responde HTTP 200 con los datos actualizados y conserva la cuenta de usuario vinculada

#### Scenario: Profesional inexistente

- **WHEN** un `ADMIN` envía `PUT /api/v1/admin/odontologos/{id}` con un identificador inexistente o dado de baja
- **THEN** el sistema responde HTTP 404 con `ErrorResponseDTO`

### Requirement: Baja lógica de odontólogo y su cuenta

The system SHALL exponer `DELETE /api/v1/admin/odontologos/{id}` para dar de baja un profesional. El sistema MUST marcar `activo = false` en el odontólogo y en su cuenta de usuario asociada en una sola operación atómica, preservando las referencias históricas. El registro MUST NOT eliminarse físicamente ni aparecer en consultas posteriores de profesionales activos.

#### Scenario: Baja desactiva profesional y cuenta

- **WHEN** un `ADMIN` envía `DELETE /api/v1/admin/odontologos/{id}` de un profesional activo
- **THEN** el odontólogo y su cuenta de usuario quedan con `activo = false`, siguen existiendo en la base de datos y dejan de aparecer en el catálogo

#### Scenario: La cuenta desactivada no puede autenticarse

- **WHEN** se intenta iniciar sesión con la cuenta del profesional dado de baja
- **THEN** el sistema rechaza la autenticación según la regla de cuentas inactivas ya definida

### Requirement: Catálogo público de profesionales

The system SHALL exponer `GET /api/v1/public/odontologos` sin exigir autenticación. El sistema MUST responder HTTP 200 con los profesionales activos cuyo payload contenga únicamente `id`, `nombre`, `apellido`, `matricula` y `especialidad`, y MUST NOT incluir `username`, `email`, contraseña ni ningún otro dato de la cuenta de usuario.

#### Scenario: Acceso público sin token

- **WHEN** un cliente no autenticado envía `GET /api/v1/public/odontologos` sin cabecera `Authorization`
- **THEN** el sistema responde HTTP 200 con la lista de profesionales activos

#### Scenario: El catálogo no expone datos de cuenta

- **WHEN** un cliente consulta el catálogo público
- **THEN** cada elemento contiene sólo `id`, `nombre`, `apellido`, `matricula` y `especialidad`, sin `username`, `email` ni contraseña

#### Scenario: Profesionales dados de baja quedan fuera del catálogo

- **WHEN** un profesional tiene `activo = false`
- **THEN** el catálogo público no lo incluye en la respuesta

### Requirement: Acceso administrativo restringido a ADMIN

The system SHALL exigir el rol `ADMIN` en los endpoints administrativos de profesionales. Un usuario autenticado sin ese rol MUST recibir HTTP 403 con `ErrorResponseDTO`, y una petición sin credenciales MUST recibir HTTP 401. Toda denegación MUST usar la estructura uniforme de error.

#### Scenario: Rol sin permiso

- **WHEN** un usuario autenticado con rol distinto de `ADMIN` intenta crear, modificar o eliminar un profesional
- **THEN** el sistema responde HTTP 403 con `ErrorResponseDTO` y no ejecuta la operación

#### Scenario: Petición sin credenciales

- **WHEN** un cliente sin token intenta acceder a un endpoint administrativo de profesionales
- **THEN** el sistema responde HTTP 401

### Requirement: Carga inicial idempotente de profesional de prueba

The system SHALL crear al arrancar un profesional de prueba con su cuenta de rol `ODONTOLOGO`, si aún no existe. La carga MUST ser idempotente entre reinicios y MUST respetar la unicidad de matrícula, username y email.

#### Scenario: Reinicio sin duplicados

- **WHEN** la aplicación se inicia dos veces contra la misma base de datos
- **THEN** existe un único profesional de prueba con su única cuenta `ODONTOLOGO`
