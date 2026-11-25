# historia-clinica Specification

## Purpose

Define el contrato verificable para la consulta de historia clínica y el registro inmutable de evoluciones odontológicas con cierre automático de turnos atendidos.

## Requirements

### Requirement: Registro de evolución clínica exclusivo para odontólogos (RN-PA-04)

The system SHALL exponer `POST /api/v1/historias-clinicas/paciente/{id}/evoluciones` accesible únicamente para usuarios autenticados con rol `ODONTOLOGO`. El sistema MUST vincular la evolución a la `HistoriaClinica` del paciente y al `Odontologo` correspondiente al usuario autenticado, respondiendo HTTP 201 con `EvolucionClinicaResponseDTO`.

#### Scenario: Alta exitosa de evolución por odontólogo

- **WHEN** un usuario con rol `ODONTOLOGO` envía `POST /api/v1/historias-clinicas/paciente/{id}/evoluciones` con `diagnostico` y `tratamientoRealizado` válidos
- **THEN** el sistema persiste la `EvolucionClinica` y responde HTTP 201 con los datos creados

#### Scenario: Rechazo por rol no autorizado

- **WHEN** un usuario con rol `ADMIN`, `RECEPCION` o `PACIENTE` intenta registrar una evolución
- **THEN** el sistema responde HTTP 403 con `ErrorResponseDTO`

### Requirement: Transición automática de turno a ATENDIDO (RN-TU-04)

The system SHALL transicionar el turno vinculado a estado `ATENDIDO` de forma atómica cuando se registra una evolución con `turnoId` válido en estado `CONFIRMADO` perteneciente al mismo paciente y odontólogo.

#### Scenario: Evolución sobre turno confirmado actualiza estado a ATENDIDO

- **WHEN** un odontólogo registra una evolución asociando un `turnoId` en estado `CONFIRMADO`
- **THEN** el turno pasa a estado `ATENDIDO` en la misma transacción

### Requirement: Inmutabilidad clínica estricta (RN-PA-03)

The system SHALL garantizar que las evoluciones clínicas solo admitan inserción (`INSERT`) y lectura (`SELECT`), bloqueando cualquier modificación o eliminación física.

#### Scenario: Bloqueo de eliminación y ausencia de endpoints mutables

- **WHEN** se intenta eliminar una `EvolucionClinica` o invocar métodos HTTP `PUT`/`DELETE` sobre evoluciones
- **THEN** el sistema rechaza la operación sin alterar los registros clínicos existentes

### Requirement: Consulta cronológica de historia clínica

The system SHALL exponer `GET /api/v1/historias-clinicas/paciente/{id}` accesible para `ODONTOLOGO`, `ADMIN` y `RECEPCION`, retornando HTTP 200 con la ficha de historia clínica y sus evoluciones ordenadas cronológicamente de la más reciente a la más antigua.