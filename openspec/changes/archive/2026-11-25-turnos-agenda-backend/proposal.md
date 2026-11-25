# Proposal

## Why

C-04 y C-05 dejaron listos los catálogos de odontólogos y pacientes, pero el sistema aún no permite consultar disponibilidad horaria ni gestionar reservas de turnos sin colisiones. Este change es crítico para el MVP porque constituye el núcleo operativo de la agenda del consultorio y desbloquea tanto la interfaz web (C-07) como el registro de evoluciones clínicas (C-08) y notificaciones (C-09).

## What Changes

- Agregar el enum \EstadoTurno\ (\PENDIENTE\, \CONFIRMADO\, \ATENDIDO\, \CANCELADO\) con validación de transiciones permitidas según RN-TU-04.
- Agregar la entidad JPA \Turno\ heredando de \AuditableEntity\ (soft delete con \@SQLDelete\ y \@SQLRestriction\) e índice \idx_odontologo_fecha\ sobre \(odontologo_id, fecha_hora_inicio, fecha_hora_fin)\.
- Agregar \TurnoRepository\ con consulta de solapamiento activo (excluyendo turnos \CANCELADO\) y filtros dinámicos por odontólogo, paciente, fecha y estado.
- Agregar DTOs (\TurnoRequestDTO\, \TurnoResponseDTO\, \DisponibilidadResponseDTO\, \SlotHorarioDTO\) sin exponer entidades JPA.
- Implementar \TurnoService\ transaccional con cálculo dinámico de disponibilidad (RN-TU-02, RN-TU-03), bloqueo pesimista anti-solapamiento (RN-TU-01) y liberación inmediata al cancelar (RN-TU-05).
- Exponer endpoints REST en \/api/v1/odontologos/{id}/disponibilidad\, \POST /api/v1/turnos\, \PUT /api/v1/turnos/{id}/cancelar\ y \GET /api/v1/turnos\.
- Cubrir todos los escenarios de negocio y concurrencia con tests automatizados sobre H2.

## Capabilities

### New Capabilities

- \	urnos\: cálculo de disponibilidad por profesional y fecha, reserva transaccional anti-solapamiento, cancelación con liberación inmediata de slot y consulta filtrada de agenda.

### Modified Capabilities

Ninguna. Se reutilizan \profesionales\, \pacientes\, \uth-session\ y el manejo uniforme de errores (\ErrorResponseDTO\).

## Impact

- **Backend código**: nuevos \model/EstadoTurno.java\, \model/Turno.java\, \epository/TurnoRepository.java\, \dto/Turno*DTO.java\, \dto/DisponibilidadResponseDTO.java\, \dto/SlotHorarioDTO.java\, \service/TurnoService.java\, \controller/TurnoController.java\, \config/TimeConfig.java\; ajuste menor en \epository/OdontologoRepository.java\ y \pplication.yml\.
- **Backend tests**: tests de repositorio, servicio (disponibilidad, transiciones, fechas pasadas, solapamiento), concurrencia multi-hilo y controller RBAC sobre H2.
- **Consumidores futuros**: desbloquea C-07 (frontend agenda), C-08 (evoluciones clínicas) y C-09 (notificaciones).
