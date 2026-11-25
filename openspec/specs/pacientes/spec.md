# pacientes Specification

## Purpose

Define el contrato observable del dominio de pacientes y mesa de entrada: alta de pacientes con creación automática de historia clínica vacía, búsqueda ágil por DNI o coincidencia de nombre/apellido para recepcionistas, consulta de ficha y baja lógica preservando la integridad de la historia clínica.

## Requirements

### Requirement: Alta de paciente con creación automática de historia clínica

The system SHALL exponer `POST /api/v1/pacientes` accesible para usuarios con roles `ADMIN` o `RECEPCION`. El request MUST incluir `dni`, `nombre`, `apellido` y `fecha_nacimiento` obligatorios, permitiendo `telefono`, `email` y `obra_social` opcionales. El sistema MUST crear de forma atómica y transaccional la entidad `Paciente` (`activo = true`) junto con una `HistoriaClinica` vacía vinculada (RN-PA-02). La respuesta MUST ser HTTP 201 con los datos del paciente creado y su historia clínica asociada.

#### Scenario: Alta exitosa crea paciente e historia clínica

- **WHEN** un usuario con rol `ADMIN` o `RECEPCION` envía `POST /api/v1/pacientes` con datos obligatorios válidos y DNI no registrado
- **THEN** el sistema responde HTTP 201 con los datos del paciente, persiste la entidad `Paciente` con `activo = true` y crea automáticamente una `HistoriaClinica` vinculada

#### Scenario: Campos obligatorios faltantes

- **WHEN** se envía un alta sin `dni`, `nombre`, `apellido` o `fecha_nacimiento`
- **THEN** el sistema responde HTTP 400 con `ErrorResponseDTO` y no persiste ningún registro

### Requirement: Rechazo de DNI duplicado

The system SHALL rechazar el alta cuando el `dni` ya se encuentre registrado en el sistema. El sistema MUST responder HTTP 409 con `ErrorResponseDTO` y no debe duplicar registros.

#### Scenario: DNI ya registrado

- **WHEN** se intenta dar de alta un paciente con un `dni` existente en la base de datos
- **THEN** el sistema responde HTTP 409 con `ErrorResponseDTO`

### Requirement: Búsqueda ágil de pacientes en mesa de entrada

The system SHALL exponer `GET /api/v1/pacientes/buscar` accesible para `ADMIN` y `RECEPCION`. El endpoint MUST permitir filtrar por `dni` exacto o realizar búsquedas por coincidencia insensible a mayúsculas/minúsculas en `nombre` o `apellido` (US-003). La respuesta MUST ser HTTP 200 con la lista de pacientes activos coincidentes.

#### Scenario: Búsqueda por DNI exacto

- **WHEN** un recepcionista consulta `GET /api/v1/pacientes/buscar?dni={dni}`
- **THEN** el sistema responde HTTP 200 con los pacientes cuyo DNI coincida exactamente

#### Scenario: Búsqueda por término en nombre o apellido

- **WHEN** un recepcionista consulta `GET /api/v1/pacientes/buscar?q={query}`
- **THEN** el sistema responde HTTP 200 con la lista de pacientes cuyo nombre o apellido contenga el término buscado

### Requirement: Consulta de ficha de paciente

The system SHALL exponer `GET /api/v1/pacientes/{id}` accesible para `ADMIN` y `RECEPCION`. El sistema MUST responder HTTP 200 con la ficha completa del paciente incluyendo los datos de su historia clínica base si el paciente existe y está activo, o HTTP 404 si no existe o está dado de baja.

#### Scenario: Consulta de paciente existente

- **WHEN** se consulta `GET /api/v1/pacientes/{id}` con un identificador válido y activo
- **THEN** el sistema responde HTTP 200 con la información del paciente y su historia clínica

#### Scenario: Paciente inexistente

- **WHEN** se consulta `GET /api/v1/pacientes/{id}` con un identificador inexistente
- **THEN** el sistema responde HTTP 404 con `ErrorResponseDTO`

### Requirement: Baja lógica (soft delete) preservando historia clínica

The system SHALL exponer `DELETE /api/v1/pacientes/{id}` accesible para `ADMIN` y `RECEPCION`. El sistema MUST marcar al paciente como inactivo (`activo = false`) sin eliminar físicamente el registro ni su `HistoriaClinica` asociada (RN-PA-05, RN-EX-01). La respuesta MUST ser HTTP 204 No Content.

#### Scenario: Baja lógica exitosa

- **WHEN** un usuario autorizado envía `DELETE /api/v1/pacientes/{id}` para un paciente activo existente
- **THEN** el sistema responde HTTP 204, establece `activo = false` en el paciente y conserva íntegra la historia clínica vinculada
