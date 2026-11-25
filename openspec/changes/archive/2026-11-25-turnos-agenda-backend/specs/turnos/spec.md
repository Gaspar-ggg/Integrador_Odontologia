# Spec Delta — Turnos y Agenda

## Purpose

Define el contrato verificable para la consulta de disponibilidad, reserva sin solapamiento, cancelación y listado filtrado de turnos odontológicos.

## ADDED Requirements

### Requirement: Cálculo dinámico de disponibilidad horaria

The system SHALL exponer \GET /api/v1/odontologos/{id}/disponibilidad?fecha=YYYY-MM-DD\ para obtener los bloques horarios libres de un odontólogo activo en la fecha indicada, excluyendo turnos activos en estado \PENDIENTE\, \CONFIRMADO\ o \ATENDIDO\ y horas ya transcurridas si la fecha es el día actual.

#### Scenario: Consulta de disponibilidad con turnos ocupados y cancelados (Caso feliz)

- **WHEN** un usuario autenticado consulta \GET /api/v1/odontologos/{id}/disponibilidad?fecha=YYYY-MM-DD\ para una fecha futura donde el profesional tiene un turno \CONFIRMADO\ de 10:00 a 10:30 y un turno \CANCELADO\ de 11:00 a 11:30
- **THEN** el sistema responde HTTP 200 excluyendo el bloque 10:00–10:30 e incluyendo como disponible el bloque 11:00–11:30

#### Scenario: Rechazo de consulta de disponibilidad en fecha pasada

- **WHEN** un usuario consulta disponibilidad para una fecha anterior a la fecha actual
- **THEN** el sistema responde HTTP 400 con \ErrorResponseDTO\

### Requirement: Reserva transaccional de turno sin solapamiento

The system SHALL exponer \POST /api/v1/turnos\ para agendar un turno validando que el paciente y el odontólogo existan y estén activos, que el horario sea futuro dentro de la franja de atención (09:00 a 18:00) con duración fija estándar (30 o 45 minutos), y que no exista solapamiento total ni parcial con otro turno no cancelado del mismo profesional.

#### Scenario: Reserva exitosa de turno disponible (Caso feliz)

- **WHEN** un usuario autorizado envía \POST /api/v1/turnos\ con \pacienteId\, \odontologoId\, \echaHoraInicio\ futura válida dentro del horario de atención y \motivoConsulta\
- **THEN** el sistema persiste el turno en estado \CONFIRMADO\ (o \PENDIENTE\ si se indica), responde HTTP 201 con \TurnoResponseDTO\ y bloquea ese intervalo en consultas posteriores

#### Scenario: Rechazo por colisión horaria total, parcial o concurrente (Error de negocio)

- **WHEN** se intenta reservar un turno para un odontólogo cuyo intervalo se superpone total o parcialmente con un turno activo existente, o dos peticiones compiten simultáneamente por el mismo slot
- **THEN** el sistema rechaza la operación en conflicto con HTTP 409 (\ErrorResponseDTO\) y no persiste el turno duplicado

#### Scenario: Reserva de turno contiguo exacto (Caso borde)

- **WHEN** existe un turno activo de 10:00 a 10:30 para un odontólogo y se solicita reservar otro turno de 10:30 a 11:00 para el mismo odontólogo
- **THEN** el sistema acepta la reserva con HTTP 201 porque los intervalos son contiguos y no se superponen

#### Scenario: Rechazo de reserva en fecha u hora pasada o fuera de horario laboral (Error de negocio)

- **WHEN** se intenta reservar un turno con \echaHoraInicio\ anterior o igual al momento actual, o fuera de la ventana 09:00–18:00
- **THEN** el sistema responde HTTP 400 con \ErrorResponseDTO\

### Requirement: Cancelación de turno y liberación inmediata de horario

The system SHALL exponer \PUT /api/v1/turnos/{id}/cancelar\ para transicionar un turno en estado \PENDIENTE\ o \CONFIRMADO\ a \CANCELADO\, liberando de inmediato su franja horaria, y MUST rechazar la cancelación si el turno ya se encuentra en estado terminal (\ATENDIDO\ o \CANCELADO\).

#### Scenario: Cancelación válida libera el slot inmediatamente

- **WHEN** un usuario autorizado envía \PUT /api/v1/turnos/{id}/cancelar\ sobre un turno \CONFIRMADO\ o \PENDIENTE\
- **THEN** el sistema actualiza el estado a \CANCELADO\, responde HTTP 200 y el bloque horario vuelve a figurar disponible y reservable

#### Scenario: Rechazo de cancelación sobre turno atendido o ya cancelado

- **WHEN** se intenta cancelar un turno cuyo estado es \ATENDIDO\ o \CANCELADO\
- **THEN** el sistema responde HTTP 400 con \ErrorResponseDTO\ sin alterar el estado

### Requirement: Listado filtrado de turnos

The system SHALL exponer \GET /api/v1/turnos\ admitiendo filtros opcionales por \odontologoId\, \pacienteId\, \echa\ (\YYYY-MM-DD\) y \estado\.

#### Scenario: Filtrado combinado de agenda

- **WHEN** un usuario autorizado consulta \GET /api/v1/turnos?odontologoId=1&fecha=2026-12-10&estado=CONFIRMADO\
- **THEN** el sistema responde HTTP 200 únicamente con los turnos activos que cumplen todos los criterios enviados
